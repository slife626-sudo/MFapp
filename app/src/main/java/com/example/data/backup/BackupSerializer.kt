package com.example.data.backup

import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryItem
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MonthlyBudget
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
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
  ): String {
    val root = JSONObject().apply {
      put("app", "Hisab")
      put("version", 3)
      put("exportedAtMillis", System.currentTimeMillis())

      val expArr = JSONArray()
      for (e in expenses) {
        expArr.put(
          JSONObject().apply {
            put("id", e.id)
            put("title", e.title)
            put("amount", e.amount)
            put("categoryId", e.categoryId)
            put("paymentMethod", e.paymentMethod)
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
            put("paymentMethod", inc.paymentMethod)
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
            put("note", pay.note)
            put("dateMillis", pay.dateMillis)
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
            put("note", pmt.note)
            put("dateMillis", pmt.dateMillis)
            put("monthKey", pmt.monthKey)
          }
        )
      }
      put("payablePayments", pmtArr)

      val catArr = JSONArray()
      for (c in customCategories) {
        catArr.put(
          JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("iconKey", c.iconKey)
            put("colorHex", c.colorHex)
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
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
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
        val amount = obj.optDouble("amount", 0.0)
        if (fromM == toM || amount <= 0.0 || amount.isNaN()) continue
        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
        val monthKey = obj.optString("monthKey", "").ifBlank {
          DateFormatUtils.monthKeyFromMillis(dateMillis)
        }
        add(
          AccountTransfer(
            id = obj.optString("id", ""),
            fromMethod = fromM.wireValue,
            toMethod = toM.wireValue,
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
        add(
          Payable(
            id = obj.optString("id", ""),
            personName = personName,
            amount = amount,
            paidAmount = obj.optDouble("paidAmount", 0.0).coerceAtLeast(0.0),
            paymentMethod = PaymentMethod.fromWire(obj.optString("paymentMethod")).wireValue,
            note = obj.optString("note", ""),
            dateMillis = dateMillis,
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
  ): String {
    val sb = StringBuilder()
    // UTF-8 BOM so Bengali & Hindi text opens cleanly in Microsoft Excel
    sb.append('\uFEFF')
    sb.append("Type,${strings.dateLabel},${strings.expenseTitleLabel},${strings.categoryLabel},${strings.paymentMethodLabel},${strings.amountLabel},Currency,${strings.noteOptionalLabel}\n")

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
        add(CsvRow(exp.dateMillis, strings.entryTypeExpense, exp.title, catName, methodLabel(exp.paymentMethod, strings), "-${exp.amount}", exp.note))
      }
      for (inc in incomes) {
        add(CsvRow(inc.dateMillis, strings.entryTypeIncome, inc.title, strings.entryTypeIncome, methodLabel(inc.paymentMethod, strings), "+${inc.amount}", inc.note))
      }
      for (tr in transfers) {
        val fromLbl = methodLabel(tr.fromMethod, strings)
        val toLbl = methodLabel(tr.toMethod, strings)
        add(CsvRow(tr.dateMillis, strings.entryTypeTransfer, "$fromLbl → $toLbl", strings.entryTypeTransfer, "$fromLbl → $toLbl", "${tr.amount}", tr.note))
      }
      for (pay in payables) {
        add(CsvRow(pay.dateMillis, strings.entryTypeBorrow, pay.personName, strings.entryTypeBorrow, methodLabel(pay.paymentMethod, strings), "+${pay.amount}", pay.note))
      }
      for (pmt in payablePayments) {
        add(CsvRow(pmt.dateMillis, strings.entryTypeRepayment, pmt.personName, strings.entryTypeRepayment, methodLabel(pmt.paymentMethod, strings), "-${pmt.amount}", pmt.note))
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

  private fun csvEscape(value: String): String {
    val clean = value.replace("\"", "\"\"")
    return "\"$clean\""
  }
}
