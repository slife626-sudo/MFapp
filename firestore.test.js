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

test("Owner can create, update, delete receivables and receivablePayments; rejects invalid and cross-user writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const created = new Date(Date.now() - 10000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("receivables").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("receivablePayments").get());

  // Valid Receivable (money lent out from BANK)
  const recRef = aliceDb.collection("users").doc(ALICE_UID).collection("receivables").doc("rec_1");
  await assertSucceeds(
    recRef.set({
      userId: ALICE_UID,
      personName: "Rahim",
      amount: 5000,
      collectedAmount: 0,
      paymentMethod: "BANK",
      note: "Short term loan",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      settled: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid ReceivablePayment (partial collection into CASH)
  const colRef = aliceDb.collection("users").doc(ALICE_UID).collection("receivablePayments").doc("col_1");
  await assertSucceeds(
    colRef.set({
      userId: ALICE_UID,
      receivableId: "rec_1",
      personName: "Rahim",
      amount: 2000,
      paymentMethod: "CASH",
      note: "Returned half",
      dateMillis: Date.now() - 2000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    recRef.update({
      collectedAmount: 2000,
      settled: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("receivables").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("receivablePayments").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("receivables").doc("rec_1").delete());

  // Owner deletes succeed
  await assertSucceeds(colRef.delete());
  await assertSucceeds(recRef.delete());
});

test("Email-shared MarketList & items: owner creates and shares with spouse email; spouse reads & checks off items; strangers and unverified emails denied", async () => {
  const wifeDb = testEnv.authenticatedContext("wife_uid", {
    email: "Wife@Example.com",
    email_verified: true,
  }).firestore();
  const husbandDb = testEnv.authenticatedContext("husband_uid", {
    email: "husband@example.com",
    email_verified: true,
  }).firestore();
  const unverifiedHusbandDb = testEnv.authenticatedContext("spoof_uid", {
    email: "husband@example.com",
    email_verified: false,
  }).firestore();
  const strangerDb = testEnv.authenticatedContext("stranger_uid", {
    email: "stranger@example.com",
    email_verified: true,
  }).firestore();

  const created = new Date(Date.now() - 10000);
  const listRef = wifeDb.collection("marketLists").doc("list_friday");

  // Unverified email cannot create
  await assertFails(
    unverifiedHusbandDb.collection("marketLists").doc("list_bad").set({
      ownerUid: "spoof_uid",
      ownerEmail: "husband@example.com",
      ownerName: "Spoof",
      title: "Fake List",
      sharedWithEmail: "",
      memberEmails: ["husband@example.com"],
      note: "",
      completed: false,
      dateMillis: Date.now(),
      createdAt: created,
      updatedAt: created,
    })
  );

  // Wife creates market list shared with husband@example.com
  await assertSucceeds(
    listRef.set({
      ownerUid: "wife_uid",
      ownerEmail: "wife@example.com",
      ownerName: "Ayesha",
      title: "Friday Bazaar",
      sharedWithEmail: "husband@example.com",
      memberEmails: ["wife@example.com", "husband@example.com"],
      note: "Buy fresh fish",
      completed: false,
      dateMillis: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Wife adds item with categoryId to the market list
  const itemRef = listRef.collection("items").doc("item_rice");
  await assertSucceeds(
    itemRef.set({
      listId: "list_friday",
      ownerUid: "wife_uid",
      memberEmails: ["wife@example.com", "husband@example.com"],
      addedByEmail: "wife@example.com",
      name: "Miniket Rice",
      quantity: "5 kg",
      estimatedPrice: 380,
      categoryId: "groceries",
      bought: false,
      note: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Unfiltered list query denied even for members
  await assertFails(husbandDb.collection("marketLists").get());

  // Query filtered by memberEmails succeeds for husband & wife
  await assertSucceeds(
    husbandDb.collection("marketLists").where("memberEmails", "array-contains", "husband@example.com").get()
  );
  await assertSucceeds(
    husbandDb.collection("marketLists").doc("list_friday").collection("items")
      .where("memberEmails", "array-contains", "husband@example.com").get()
  );

  // Spoofed unverified email denied read
  await assertFails(
    unverifiedHusbandDb.collection("marketLists").doc("list_friday").get()
  );

  // Stranger denied read, write, and delete
  await assertFails(strangerDb.collection("marketLists").doc("list_friday").get());
  await assertFails(
    strangerDb.collection("marketLists").where("memberEmails", "array-contains", "husband@example.com").get()
  );
  await assertFails(
    strangerDb.collection("marketLists").doc("list_friday").collection("items").doc("item_rice").update({
      bought: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Husband marks item as bought with actual price & updated categoryId & completes list
  await assertSucceeds(
    husbandDb.collection("marketLists").doc("list_friday").collection("items").doc("item_rice").update({
      bought: true,
      estimatedPrice: 390,
      categoryId: "food",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    husbandDb.collection("marketLists").doc("list_friday").update({
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Husband (non-owner) cannot hijack ownerEmail or delete owner's list
  await assertFails(husbandDb.collection("marketLists").doc("list_friday").delete());
  await assertFails(
    husbandDb.collection("marketLists").doc("list_friday").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Owner (wife) can delete item & list
  await assertSucceeds(itemRef.delete());
  await assertSucceeds(listRef.delete());
});

test("Custom accounts, cross-account transfers, optional fields, and 30-day trash recycle bin", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const created = new Date(Date.now() - 10000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("accounts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("trash").get());

  // Create two custom BANK accounts
  const acctARef = aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("acct_dbbl");
  const acctBRef = aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("acct_city");
  await assertSucceeds(
    acctARef.set({
      userId: ALICE_UID,
      name: "DBBL Savings",
      accountType: "BANK",
      iconKey: "bank",
      colorHex: 0xFF2563EB,
      openingBalance: 10000,
      currencyCode: "BDT",
      custom: true,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    acctBRef.set({
      userId: ALICE_UID,
      name: "City Bank",
      accountType: "BANK",
      iconKey: "bank",
      colorHex: 0xFF0E7C5B,
      openingBalance: 5000,
      currencyCode: "BDT",
      custom: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Update custom account
  await assertSucceeds(
    acctARef.update({
      name: "DBBL Salary",
      openingBalance: 12000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Expense & Income with optional accountId & categoryId
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_acct").set({
      userId: ALICE_UID,
      title: "Grocery",
      amount: 550,
      categoryId: "groceries",
      paymentMethod: "BANK",
      accountId: "acct_dbbl",
      note: "",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("incomes").doc("inc_acct").set({
      userId: ALICE_UID,
      title: "Monthly Salary",
      amount: 35000,
      categoryId: "inc_salary",
      paymentMethod: "BANK",
      accountId: "acct_dbbl",
      note: "Credited",
      dateMillis: Date.now() - 5000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Cross-account transfer between two different custom BANK accounts succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transfers").doc("tr_custom").set({
      userId: ALICE_UID,
      fromMethod: "BANK",
      toMethod: "BANK",
      fromAccountId: "acct_dbbl",
      toAccountId: "acct_city",
      amount: 2000,
      note: "Rebalance bank",
      dateMillis: Date.now() - 4000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Same account transfer without distinct accountIds is denied
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transfers").doc("tr_same_acct").set({
      userId: ALICE_UID,
      fromMethod: "BANK",
      toMethod: "BANK",
      fromAccountId: "acct_dbbl",
      toAccountId: "acct_dbbl",
      amount: 500,
      note: "",
      dateMillis: Date.now() - 4000,
      monthKey: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Payable & Receivable with optional dueDateMillis and accountId, plus CustomCategory with categoryType
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payables").doc("pay_due").set({
      userId: ALICE_UID,
      personName: "Hasan",
      amount: 4000,
      paidAmount: 0,
      paymentMethod: "BANK",
      accountId: "acct_dbbl",
      note: "",
      dateMillis: Date.now() - 5000,
      dueDateMillis: Date.now() + 86400000,
      monthKey: "2026-11",
      settled: false,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_inc").set({
      userId: ALICE_UID,
      name: "Bonus",
      iconKey: "trophy",
      colorHex: 0xFFD97706,
      categoryType: "INCOME",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Trash create & delete succeed; update denied
  const trashRef = aliceDb.collection("users").doc(ALICE_UID).collection("trash").doc("tr_1");
  await assertSucceeds(
    trashRef.set({
      userId: ALICE_UID,
      entityType: "EXPENSE",
      sourceId: "exp_acct",
      title: "Grocery",
      amount: 550,
      deletedAtMillis: Date.now(),
      payload: {
        title: "Grocery",
        amount: 550,
        categoryId: "groceries",
        paymentMethod: "BANK",
        accountId: "acct_dbbl",
        note: "",
        dateMillis: Date.now() - 5000,
        monthKey: "2026-11",
      },
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(
    trashRef.update({
      title: "Modified",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation on accounts and trash
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("accounts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("trash").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("trash").doc("tr_1").delete());

  // Owner deletes trash item & accounts
  await assertSucceeds(trashRef.delete());
  await assertSucceeds(acctARef.delete());
  await assertSucceeds(acctBRef.delete());
});

