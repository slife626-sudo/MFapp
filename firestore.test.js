const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are denied read and write on all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("expenses").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("categories").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("budgets").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_1").set({
      userId: ALICE_UID,
      title: "Coffee",
      amount: 120,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner: valid expense create, list, partial update, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const expRef = aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_1");

  await assertSucceeds(
    expRef.set({
      userId: ALICE_UID,
      title: "Grocery Market",
      amount: 850.5,
      categoryId: "groceries",
      paymentMethod: "MOBILE",
      note: "Vegetables and rice",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // List empty or populated subcollection works without existing() errors
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").get()
  );

  // Partial update preserving immutable createdAt and userId
  await assertSucceeds(
    expRef.update({
      title: "Weekly Grocery",
      amount: 920.0,
      categoryId: "groceries",
      paymentMethod: "CARD",
      note: "Updated receipt",
      dateMillis: Date.now() - 4000,
      monthKey: "2026-11",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Delete succeeds for owner
  await assertSucceeds(expRef.delete());
});

test("Rejects invalid expense payloads, future timestamps, extra @DocumentId field, and ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Reject @DocumentId `id` inside payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_bad_id").set({
      id: "exp_bad_id",
      userId: ALICE_UID,
      title: "Tea",
      amount: 20,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject negative amount
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_neg").set({
      userId: ALICE_UID,
      title: "Tea",
      amount: -5,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_future").set({
      userId: ALICE_UID,
      title: "Tea",
      amount: 50,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );

  // Reject invalid monthKey format
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_month").set({
      userId: ALICE_UID,
      title: "Tea",
      amount: 50,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "Nov2026",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Cross-user isolation: Bob cannot read or write Alice's expenses, categories, or budgets", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(ALICE_UID).collection("expenses").doc("a_exp").set({
      userId: ALICE_UID,
      title: "Lunch",
      amount: 200,
      categoryId: "food",
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    });
    await db.collection("users").doc(ALICE_UID).collection("budgets").doc("2026-11").set({
      userId: ALICE_UID,
      monthKey: "2026-11",
      totalLimit: 25000,
      categoryLimits: { food: 6000 },
      currencyCode: "BDT",
      createdAt: created,
      updatedAt: created,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("expenses").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("expenses").doc("a_exp").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("expenses").doc("a_exp").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("budgets").doc("2026-11").get());
});

test("Owner can create and update custom categories and monthly budgets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  const catRef = aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_pet");
  await assertSucceeds(
    catRef.set({
      userId: ALICE_UID,
      name: "Pet Care",
      iconKey: "pets",
      colorHex: 0xFF00897B,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    catRef.update({
      name: "Pet Supplies",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  const budgetRef = aliceDb.collection("users").doc(ALICE_UID).collection("budgets").doc("2026-11");
  await assertSucceeds(
    budgetRef.set({
      userId: ALICE_UID,
      monthKey: "2026-11",
      totalLimit: 30000,
      categoryLimits: { food: 8000, transport: 3500 },
      currencyCode: "BDT",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    budgetRef.update({
      totalLimit: 35000,
      categoryLimits: { food: 9000, transport: 4000 },
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can create, update, and delete wallet balance/income entries; cross-user and invalid writes denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const created = new Date(Date.now() - 10000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("incomes").get());

  const incRef = aliceDb.collection("users").doc(ALICE_UID).collection("incomes").doc("inc_1");
  await assertSucceeds(
    incRef.set({
      userId: ALICE_UID,
      title: "Monthly Salary",
      amount: 1000,
      paymentMethod: "CASH",
      note: "Opening balance",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("incomes").get());

  await assertSucceeds(
    incRef.update({
      title: "Opening Wallet Balance",
      amount: 1500,
      paymentMethod: "MOBILE",
      note: "Topped up",
      dateMillis: Date.now() - 4000,
      monthKey: "2026-11",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject negative amount
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("incomes").doc("inc_bad").set({
      userId: ALICE_UID,
      title: "Invalid",
      amount: 0,
      paymentMethod: "CASH",
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("incomes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("incomes").doc("inc_1").delete());

  // Owner delete succeeds
  await assertSucceeds(incRef.delete());
});

test("Owner can create, update, delete transfers, payables, and payablePayments; rejects invalid and cross-user writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const created = new Date(Date.now() - 10000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transfers").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("payables").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("payablePayments").get());

  // Valid transfer: BANK -> CASH
  const trRef = aliceDb.collection("users").doc(ALICE_UID).collection("transfers").doc("tr_1");
  await assertSucceeds(
    trRef.set({
      userId: ALICE_UID,
      fromMethod: "BANK",
      toMethod: "CASH",
      amount: 5000,
      note: "Withdraw to wallet",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject same-account transfer (CASH -> CASH)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transfers").doc("tr_same").set({
      userId: ALICE_UID,
      fromMethod: "CASH",
      toMethod: "CASH",
      amount: 500,
      note: "",
      dateMillis: Date.now(),
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update on transfer
  await assertSucceeds(
    trRef.update({
      fromMethod: "BANK",
      toMethod: "MOBILE",
      amount: 3000,
      note: "Updated to bKash",
      dateMillis: Date.now() - 4000,
      monthKey: "2026-11",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid Payable (borrowed money)
  const payRef = aliceDb.collection("users").doc(ALICE_UID).collection("payables").doc("pay_1");
  await assertSucceeds(
    payRef.set({
      userId: ALICE_UID,
      personName: "Karim",
      amount: 3000,
      paidAmount: 0,
      paymentMethod: "MOBILE",
      note: "Emergency loan",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      settled: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid PayablePayment (partial repayment)
  const repayRef = aliceDb.collection("users").doc(ALICE_UID).collection("payablePayments").doc("repay_1");
  await assertSucceeds(
    repayRef.set({
      userId: ALICE_UID,
      payableId: "pay_1",
      personName: "Karim",
      amount: 1000,
      paymentMethod: "BANK",
      note: "First installment",
      dateMillis: Date.now() - 2000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Update Payable paidAmount
  await assertSucceeds(
    payRef.update({
      paidAmount: 1000,
      settled: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transfers").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("payables").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("payablePayments").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("payables").doc("pay_1").delete());

  // Owner deletes succeed
  await assertSucceeds(repayRef.delete());
  await assertSucceeds(payRef.delete());
  await assertSucceeds(trRef.delete());
});
