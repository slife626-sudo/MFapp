package com.example.data.backup

import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryFlowType
import com.example.data.model.CategoryItem
import com.example.data.model.CustomAccount
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MonthlyBudget
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.i18n.AppStrings
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
  val expenses: List<Expense>,
  val customCategories: List<CustomCategory>,
  val budgets: List<MonthlyBudget>,
  val incomes: List<Income> = emptyList(),
  val transfers: List<AccountTransfer> = emptyList(),
  val payables: List<Payable> = emptyList(),
  val payablePayments: List<PayablePayment> = emptyList(),
  val receivables: List<Receivable> = emptyList(),
  val receivablePayments: List<ReceivablePayment> = emptyList(),
  val customAccounts: List<CustomAccount> = emptyList(),
)

data class LedgerCsvRow(
  val dateMillis: Long,
  val particulars: String,
  val accountName: String,
  val categoryOrType: String,
  val receiptAmount: Double,
  val paymentAmount: Double,
  val runningBalance: Double,
)

data class TrialBalanceCsvRow(
  val groupName: String,
  val accountHead: String,
  val debitAmount: Double,
  val creditAmount: Double,
)

object BackupSerializer {

  fun toJsonBackup(
    expenses: List<Expense>,
    customCategories: List<CustomCategory>,
    budgets: List<MonthlyBudget>,
    incomes: List<Income> = emptyList(),
    transfers: List<AccountTransfer> = emptyList(),
    payables: List<Payable> = emptyList(),
    payablePayments: List<PayablePayment> = emptyList(),
    receivables: List<Receivable> = emptyList(),
    receivablePayments: List<ReceivablePayment> = emptyList(),
    customAccounts: List<CustomAccount> = emptyList(),
  ): String {
    val root = JSONObject().apply {
      put("app", "Hisab")
      put("version", 5)
      put("exportedAtMillis", System.currentTimeMillis())

      val acctArr = JSONArray()
      for (acct in customAccounts) {
        acctArr.put(
          JSONObject().apply {
            put("id", acct.id)
            put("name", acct.name)
            put("accountType", acct.accountType)
            put("iconKey", acct.iconKey)
            put("colorHex", acct.colorHex)
            put("openingBalance", acct.openingBalance)
            put("currencyCode", acct.currencyCode)
            put("custom", acct.custom)
          }
        )
      }
      put("customAccounts", acctArr)

      val expArr = JSONArray()
      for (e in expenses) {
        expArr.put(
          JSONObject().apply {
            put("id", e.id)
            put("title", e.title)
            put("amount", e.amount)
            put("categoryId", e.categoryId)
            put("paymentMethod", e.paymentMethod)
            e.accountId?.let { put("accountId", it) }
            put("note", e.note)
            put("dateMillis", e.dateMillis)
            put("monthKey", e.monthKey)
          }
        )
      }
      put("expenses", expArr)

      val incArr = JSONArray()
      for (inc in incomes) {
        incArr.put(
          JSONObject().apply {
            put("id", inc.id)
            put("title", inc.title)
            put("amount", inc.amount)
            inc.categoryId?.let { put("categoryId", it) }
            put("paymentMethod", inc.paymentMethod)
            inc.accountId?.let { put("accountId", it) }
            put("note", inc.note)
            put("dateMillis", inc.dateMillis)
            put("monthKey", inc.monthKey)
          }
        )
      }
      put("incomes", incArr)

      val trArr = JSONArray()
      for (tr in transfers) {
        trArr.put(
          JSONObject().apply {
            put("id", tr.id)
            put("fromMethod", tr.fromMethod)
            put("toMethod", tr.toMethod)
            tr.fromAccountId?.let { put("fromAccountId", it) }
            tr.toAccountId?.let { put("toAccountId", it) }
            put("amount", tr.amount)
            put("note", tr.note)
            put("dateMillis", tr.dateMillis)
            put("monthKey", tr.monthKey)
          }
        )
      }
      put("transfers", trArr)

      val payArr = JSONArray()
      for (pay in payables) {
        payArr.put(
          JSONObject().apply {
            put("id", pay.id)
            put("personName", pay.personName)
            put("amount", pay.amount)
            put("paidAmount", pay.paidAmount)
            put("paymentMethod", pay.paymentMethod)
            pay.accountId?.let { put("accountId", it) }
            put("note", pay.note)
            put("dateMillis", pay.dateMillis)
            pay.dueDateMillis?.let { put("dueDateMillis", it) }
            put("monthKey", pay.monthKey)
            put("settled", pay.settled)
          }
        )
      }
      put("payables", payArr)

      val pmtArr = JSONArray()
      for (pmt in payablePayments) {
        pmtArr.put(
          JSONObject().apply {
            put("id", pmt.id)
            put("payableId", pmt.payableId)
            put("personName", pmt.personName)
            put("amount", pmt.amount)
            put("paymentMethod", pmt.paymentMethod)
            pmt.accountId?.let { put("accountId", it) }
            put("note", pmt.note)
            put("dateMillis", pmt.dateMillis)
            put("monthKey", pmt.monthKey)
          }
        )
      }
      put("payablePayments", pmtArr)

      val recArr = JSONArray()
      for (rec in receivables) {
        recArr.put(
          JSONObject().apply {
            put("id", rec.id)
            put("personName", rec.personName)
            put("amount", rec.amount)
            put("collectedAmount", rec.collectedAmount)
            put("paymentMethod", rec.paymentMethod)
            rec.accountId?.let { put("accountId", it) }
            put("note", rec.note)
            put("dateMillis", rec.dateMillis)
            rec.dueDateMillis?.let { put("dueDateMillis", it) }
            put("monthKey", rec.monthKey)
            put("settled", rec.settled)
          }
        )
      }
      put("receivables", recArr)

      val recPmtArr = JSONArray()
      for (pmt in receivablePayments) {
        recPmtArr.put(
          JSONObject().apply {
            put("id", pmt.id)
            put("receivableId", pmt.receivableId)
            put("personName", pmt.personName)
            put("amount", pmt.amount)
            put("paymentMethod", pmt.paymentMethod)
            pmt.accountId?.let { put("accountId", it) }
            put("note", pmt.note)
            put("dateMillis", pmt.dateMillis)
            put("monthKey", pmt.monthKey)
          }
        )
      }
      put("receivablePayments", recPmtArr)

      val catArr = JSONArray()
      for (c in customCategories) {
        catArr.put(
          JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("iconKey", c.iconKey)
            put("colorHex", c.colorHex)
            put("categoryType", c.categoryType)
          }
        )
      }
      put("categories", catArr)

      val budgetArr = JSONArray()
      for (b in budgets) {
        val catLimitsObj = JSONObject()
        for ((k, v) in b.categoryLimits) {
          catLimitsObj.put(k, v)
        }
        budgetArr.put(
          JSONObject().apply {
            put("monthKey", b.monthKey)
            put("totalLimit", b.totalLimit)
            put("currencyCode", b.currencyCode)
            put("categoryLimits", catLimitsObj)
          }
        )
      }
      put("budgets", budgetArr)
    }
    return root.toString(2)
  }

  fun parseJsonBackup(jsonText: String): BackupPayload {
    val root = JSONObject(jsonText)

    val acctArr = root.optJSONArray("customAccounts") ?: JSONArray()
    val customAccounts = buildList {
      for (i in 0 until acctArr.length()) {
        val obj = acctArr.optJSONObject(i) ?: continue
        val name = obj.optString("name", "").trim()
        if (name.isEmpty()) continue
        add(
          CustomAccount(
            id = obj.optString("id", ""),
            name = name,
            accountType = PaymentMethod.fromWire(obj.optString("accountType")).wireValue,
            iconKey = obj.optString("iconKey", "wallet"),
            colorHex = obj.optLong("colorHex", 0xFF2563EB),
            openingBalance = obj.optDouble("openingBalance", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            currencyCode = obj.optString("currencyCode", "BDT").ifBlank { "BDT" },
            custom = obj.optBoolean("custom", true),
          )
        )
      }
    }

    val expArr = root.optJSONArray("expenses") ?: JSONArray()
    val expenses = buildList {
      for (i in 0 until expArr.length()) {
        val obj = expArr.optJSONObject(i) ?: continue
        val title = obj.optString("title", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (title.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          Expense(
            id = obj.optString("id", ""),
            title = title,
            amount = amount,
            categoryId = obj.optString("categoryId", "other").ifBlank { "other" },
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            monthKey = monthKey,
          )
        )
      }
    }

    val incArr = root.optJSONArray("incomes") ?: JSONArray()
    val incomes = buildList {
      for (i in 0 until incArr.length()) {
        val obj = incArr.optJSONObject(i) ?: continue
        val title = obj.optString("title", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (title.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          Income(
            id = obj.optString("id", ""),
            title = title,
            amount = amount,
            categoryId = obj.optString("categoryId", "").ifBlank { null },
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            monthKey = monthKey,
          )
        )
      }
    }

    val trArr = root.optJSONArray("transfers") ?: JSONArray()
    val transfers = buildList {
      for (i in 0 until trArr.length()) {
        val obj = trArr.optJSONObject(i) ?: continue
        val fromM = PaymentMethod.fromWire(obj.optString("fromMethod"))
        val toM = PaymentMethod.fromWire(obj.optString("toMethod"))
        val fromAcc = obj.optString("fromAccountId", "").ifBlank { null }
        val toAcc = obj.optString("toAccountId", "").ifBlank { null }
        val amount = obj.optDouble("amount", 0.0)
        val distinctAccs = fromAcc != null && toAcc != null && fromAcc != toAcc
        if ((fromM == toM && !distinctAccs) || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          AccountTransfer(
            id = obj.optString("id", ""),
            fromMethod = fromM.wireValue,
            toMethod = toM.wireValue,
            fromAccountId = fromAcc,
            toAccountId = toAcc,
            amount = amount,
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            monthKey = monthKey,
          )
        )
      }
    }

    val payArr = root.optJSONArray("payables") ?: JSONArray()
    val payables = buildList {
      for (i in 0 until payArr.length()) {
        val obj = payArr.optJSONObject(i) ?: continue
        val personName = obj.optString("personName", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (personName.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        val due = obj.optLong("dueDateMillis", 0L).takeIf { it > 0L }
        add(
          Payable(
            id = obj.optString("id", ""),
            personName = personName,
            amount = amount,
            paidAmount = obj.optDouble("paidAmount", 0.0).coerceAtLeast(0.0),
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            dueDateMillis = due,
            monthKey = monthKey,
            settled = obj.optBoolean("settled", false),
          )
        )
      }
    }

    val pmtArr = root.optJSONArray("payablePayments") ?: JSONArray()
    val payablePayments = buildList {
      for (i in 0 until pmtArr.length()) {
        val obj = pmtArr.optJSONObject(i) ?: continue
        val payableId = obj.optString("payableId", "").trim()
        val personName = obj.optString("personName", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (payableId.isEmpty() || personName.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          PayablePayment(
            id = obj.optString("id", ""),
            payableId = payableId,
            personName = personName,
            amount = amount,
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            monthKey = monthKey,
          )
        )
      }
    }

    val recArr = root.optJSONArray("receivables") ?: JSONArray()
    val receivables = buildList {
      for (i in 0 until recArr.length()) {
        val obj = recArr.optJSONObject(i) ?: continue
        val personName = obj.optString("personName", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (personName.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        val due = obj.optLong("dueDateMillis", 0L).takeIf { it > 0L }
        add(
          Receivable(
            id = obj.optString("id", ""),
            personName = personName,
            amount = amount,
            collectedAmount = obj.optDouble("collectedAmount", 0.0).coerceAtLeast(0.0),
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            dueDateMillis = due,
            monthKey = monthKey,
            settled = obj.optBoolean("settled", false),
          )
        )
      }
    }

    val recPmtArr = root.optJSONArray("receivablePayments") ?: JSONArray()
    val receivablePayments = buildList {
      for (i in 0 until recPmtArr.length()) {
        val obj = recPmtArr.optJSONObject(i) ?: continue
        val recId = obj.optString("receivableId", "").trim()
        val personName = obj.optString("personName", "").trim()
        val amount = obj.optDouble("amount", 0.0)
        if (recId.isEmpty() || personName.isEmpty() || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          ReceivablePayment(
            id = obj.optString("id", ""),
            receivableId = recId,
            personName = personName,
            amount = amount,
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            accountId = obj.optString("accountId", "").ifBlank { null },
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
            monthKey = monthKey,
          )
        )
      }
    }

    val catArr = root.optJSONArray("categories") ?: JSONArray()
    val categories = buildList {
      for (i in 0 until catArr.length()) {
        val obj = catArr.optJSONObject(i) ?: continue
        val name = obj.optString("name", "").trim()
        if (name.isEmpty()) continue
        add(
          CustomCategory(
            id = obj.optString("id", ""),
            name = name,
            iconKey = obj.optString("iconKey", "other"),
            colorHex = obj.optLong("colorHex", 0xFF0E7C5B),
            categoryType = CategoryFlowType.fromWire(obj.optString("categoryType", "EXPENSE")).wireValue,
          )
        )
      }
    }

    val budgetArr = root.optJSONArray("budgets") ?: JSONArray()
    val budgets = buildList {
      for (i in 0 until budgetArr.length()) {
        val obj = budgetArr.optJSONObject(i) ?: continue
        val monthKey = obj.optString("monthKey", "").trim()
        if (monthKey.isEmpty()) continue
        val limitsObj = obj.optJSONObject("categoryLimits") ?: JSONObject()
        val limitsMap = mutableMapOf<String, Double>()
        limitsObj.keys().forEach { key ->
          val v = limitsObj.optDouble(key, -1.0)
          if (v >= 0.0 && !v.isNaN()) {
            limitsMap[key] = v
          }
        }
        add(
          MonthlyBudget(
            id = monthKey,
            monthKey = monthKey,
            totalLimit = obj.optDouble("totalLimit", 0.0).coerceAtLeast(0.0),
            categoryLimits = limitsMap,
            currencyCode = obj.optString("currencyCode", "BDT"),
          )
        )
      }
    }

    return BackupPayload(
      expenses = expenses,
      customCategories = categories,
      budgets = budgets,
      incomes = incomes,
      transfers = transfers,
      payables = payables,
      payablePayments = payablePayments,
      receivables = receivables,
      receivablePayments = receivablePayments,
      customAccounts = customAccounts,
    )
  }

  private fun methodLabel(methodWire: String, strings: AppStrings): String =
    when (PaymentMethod.fromWire(methodWire)) {
      PaymentMethod.CASH -> strings.cash
      PaymentMethod.CARD -> strings.card
      PaymentMethod.MOBILE -> strings.mobileBanking
      PaymentMethod.BANK -> strings.bank
    }

  fun toCsvExport(
    expenses: List<Expense>,
    categoriesById: Map<String, CategoryItem>,
    currencySymbol: String,
    strings: AppStrings,
    langCode: String,
    incomes: List<Income> = emptyList(),
    transfers: List<AccountTransfer> = emptyList(),
    payables: List<Payable> = emptyList(),
    payablePayments: List<PayablePayment> = emptyList(),
    receivables: List<Receivable> = emptyList(),
    receivablePayments: List<ReceivablePayment> = emptyList(),
    accountsById: Map<String, CustomAccount> = emptyMap(),
  ): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')
    sb.append("Type,${strings.dateLabel},${strings.expenseTitleLabel},${strings.categoryLabel},${strings.paymentMethodLabel},${strings.amountLabel},Currency,${strings.noteOptionalLabel}\n")

    fun accountDisplay(accountId: String?, methodWire: String): String =
      accountId?.let { accountsById[it]?.name } ?: methodLabel(methodWire, strings)

    data class CsvRow(
      val dateMillis: Long,
      val typeLabel: String,
      val title: String,
      val category: String,
      val method: String,
      val signedAmount: String,
      val note: String,
    )

    val rows = buildList {
      for (exp in expenses) {
        val catName = categoriesById[exp.categoryId]?.name ?: exp.categoryId
        add(CsvRow(exp.dateMillis, strings.entryTypeExpense, exp.title, catName, accountDisplay(exp.accountId, exp.paymentMethod), "-${exp.amount}", exp.note))
      }
      for (inc in incomes) {
        val catName = inc.categoryId?.let { categoriesById[it]?.name } ?: strings.entryTypeIncome
        add(CsvRow(inc.dateMillis, strings.entryTypeIncome, inc.title, catName, accountDisplay(inc.accountId, inc.paymentMethod), "+${inc.amount}", inc.note))
      }
      for (tr in transfers) {
        val fromLbl = accountDisplay(tr.fromAccountId, tr.fromMethod)
        val toLbl = accountDisplay(tr.toAccountId, tr.toMethod)
        add(CsvRow(tr.dateMillis, strings.entryTypeTransfer, "$fromLbl -> $toLbl", strings.entryTypeTransfer, "$fromLbl -> $toLbl", "${tr.amount}", tr.note))
      }
      for (pay in payables) {
        add(CsvRow(pay.dateMillis, strings.entryTypeBorrow, pay.personName, strings.entryTypeBorrow, accountDisplay(pay.accountId, pay.paymentMethod), "+${pay.amount}", pay.note))
      }
      for (pmt in payablePayments) {
        add(CsvRow(pmt.dateMillis, strings.entryTypeRepayment, pmt.personName, strings.entryTypeRepayment, accountDisplay(pmt.accountId, pmt.paymentMethod), "-${pmt.amount}", pmt.note))
      }
      for (rec in receivables) {
        add(CsvRow(rec.dateMillis, strings.entryTypeLend, rec.personName, strings.entryTypeLend, accountDisplay(rec.accountId, rec.paymentMethod), "-${rec.amount}", rec.note))
      }
      for (pmt in receivablePayments) {
        add(CsvRow(pmt.dateMillis, strings.entryTypeCollection, pmt.personName, strings.entryTypeCollection, accountDisplay(pmt.accountId, pmt.paymentMethod), "+${pmt.amount}", pmt.note))
      }
    }.sortedByDescending { it.dateMillis }

    for (row in rows) {
      val dateStr = DateFormatUtils.formatDateCompact(row.dateMillis, langCode)
      sb.append(csvEscape(row.typeLabel)).append(',')
      sb.append(csvEscape(dateStr)).append(',')
      sb.append(csvEscape(row.title)).append(',')
      sb.append(csvEscape(row.category)).append(',')
      sb.append(csvEscape(row.method)).append(',')
      sb.append(row.signedAmount).append(',')
      sb.append(csvEscape(currencySymbol)).append(',')
      sb.append(csvEscape(row.note)).append('\n')
    }
    return sb.toString()
  }

  fun toLedgerCsv(
    rows: List<LedgerCsvRow>,
    currencySymbol: String,
    langCode: String,
    strings: AppStrings,
  ): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')
    sb.append("${strings.dateLabel},Particulars,${strings.accountsLabel},${strings.categoryLabel},${strings.receiptsLabel},${strings.paymentsLabel},${strings.runningBalanceLabel},Currency\n")
    for (row in rows) {
      val dateStr = DateFormatUtils.formatDateCompact(row.dateMillis, langCode)
      sb.append(csvEscape(dateStr)).append(',')
      sb.append(csvEscape(row.particulars)).append(',')
      sb.append(csvEscape(row.accountName)).append(',')
      sb.append(csvEscape(row.categoryOrType)).append(',')
      sb.append(if (row.receiptAmount > 0.0) row.receiptAmount else "").append(',')
      sb.append(if (row.paymentAmount > 0.0) row.paymentAmount else "").append(',')
      sb.append(row.runningBalance).append(',')
      sb.append(csvEscape(currencySymbol)).append('\n')
    }
    return sb.toString()
  }

  fun toTrialBalanceCsv(
    rows: List<TrialBalanceCsvRow>,
    totalDebit: Double,
    totalCredit: Double,
    currencySymbol: String,
    strings: AppStrings,
  ): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')
    sb.append("Group,Account Head,${strings.debitLabel},${strings.creditLabel},Currency\n")
    for (row in rows) {
      sb.append(csvEscape(row.groupName)).append(',')
      sb.append(csvEscape(row.accountHead)).append(',')
      sb.append(if (row.debitAmount > 0.0) row.debitAmount else "").append(',')
      sb.append(if (row.creditAmount > 0.0) row.creditAmount else "").append(',')
      sb.append(csvEscape(currencySymbol)).append('\n')
    }
    sb.append(csvEscape("Total")).append(',')
    sb.append(csvEscape("Total")).append(',')
    sb.append(totalDebit).append(',')
    sb.append(totalCredit).append(',')
    sb.append(csvEscape(currencySymbol)).append('\n')
    return sb.toString()
  }

  private fun csvEscape(value: String): String {
    val clean = value.replace("\"", "\"\"")
    return "\"$clean\""
  }
}
