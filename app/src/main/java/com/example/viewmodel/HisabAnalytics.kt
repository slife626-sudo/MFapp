package com.example.viewmodel

import com.example.data.backup.LedgerCsvRow
import com.example.data.backup.TrialBalanceCsvRow
import com.example.data.model.AccountSummary
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
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
import com.example.data.model.PeriodFilter
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.TrashItem
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.stringsFor
import java.util.Calendar
import kotlin.math.abs

data class CategoryBreakdown(
  val category: CategoryItem,
  val totalAmount: Double,
  val percentage: Float,
  val transactionCount: Int,
  val budgetLimit: Double,
)

data class MonthlySummary(
  val monthKey: String,
  val totalSpent: Double,
  val totalIncome: Double,
  val netBalance: Double,
  val prevMonthSpent: Double,
  val transactionCount: Int,
  val dailyAverage: Double,
  val topCategory: CategoryBreakdown?,
  val budgetLimit: Double,
  val categoryBreakdowns: List<CategoryBreakdown>,
  val dailyTotals: Map<Int, Double>,
  val sixMonthTotals: List<Pair<String, Double>>,
)

data class PeriodAnalyticsSummary(
  val filter: PeriodFilter,
  val monthKey: String,
  val startMillis: Long,
  val endMillis: Long,
  val totalIncome: Double,
  val totalSpent: Double,
  val netIncome: Double,
  val savingsRate: Float,
  val prevPeriodSpent: Double,
  val transactionCount: Int,
  val dailyAverage: Double,
  val topCategory: CategoryBreakdown?,
  val budgetLimit: Double,
  val expenseBreakdowns: List<CategoryBreakdown>,
  val incomeBreakdowns: List<CategoryBreakdown>,
  val dailyTotals: Map<Int, Double>,
  val incomeVsExpenseMonthly: List<Triple<String, Double, Double>>,
  val netCashFlowSeries: List<Pair<String, Double>>,
  val todaySpent: Double,
  val todayIncome: Double,
  val todayCount: Int,
)

data class LedgerEntry(
  val dateMillis: Long,
  val title: String,
  val accountName: String,
  val categoryOrType: String,
  val receiptAmount: Double,
  val paymentAmount: Double,
  val runningBalance: Double,
  val entityType: String,
)

data class AccountLedgerResult(
  val openingBalance: Double,
  val totalReceipts: Double,
  val totalPayments: Double,
  val netChange: Double,
  val closingBalance: Double,
  val entries: List<LedgerEntry>,
) {
  fun toCsvRows(): List<LedgerCsvRow> = entries.map {
    LedgerCsvRow(
      dateMillis = it.dateMillis,
      particulars = it.title,
      accountName = it.accountName,
      categoryOrType = it.categoryOrType,
      receiptAmount = it.receiptAmount,
      paymentAmount = it.paymentAmount,
      runningBalance = it.runningBalance,
    )
  }
}

data class TrialBalanceLine(
  val accountHead: String,
  val debitAmount: Double,
  val creditAmount: Double,
)

data class TrialBalanceGroup(
  val groupName: String,
  val lines: List<TrialBalanceLine>,
  val totalDebit: Double,
  val totalCredit: Double,
)

data class TrialBalanceResult(
  val groups: List<TrialBalanceGroup>,
  val totalDebit: Double,
  val totalCredit: Double,
  val isBalanced: Boolean,
) {
  fun toCsvRows(): List<TrialBalanceCsvRow> = groups.flatMap { group ->
    group.lines.map { line ->
      TrialBalanceCsvRow(
        groupName = group.groupName,
        accountHead = line.accountHead,
        debitAmount = line.debitAmount,
        creditAmount = line.creditAmount,
      )
    }
  }
}

data class AccountBalances(
  val cash: Double = 0.0,
  val bank: Double = 0.0,
  val mobile: Double = 0.0,
  val card: Double = 0.0,
) {
  fun forMethod(method: PaymentMethod): Double = when (method) {
    PaymentMethod.CASH -> cash
    PaymentMethod.BANK -> bank
    PaymentMethod.MOBILE -> mobile
    PaymentMethod.CARD -> card
  }

  val total: Double get() = cash + bank + mobile + card
}

data class DashboardUiData(
  val expenses: List<Expense>,
  val incomes: List<Income>,
  val transfers: List<AccountTransfer> = emptyList(),
  val payables: List<Payable> = emptyList(),
  val payablePayments: List<PayablePayment> = emptyList(),
  val receivables: List<Receivable> = emptyList(),
  val receivablePayments: List<ReceivablePayment> = emptyList(),
  val customCategories: List<CustomCategory>,
  val budgets: List<MonthlyBudget>,
  val hasPendingWrites: Boolean,
  val isFromCache: Boolean,
  val customAccounts: List<CustomAccount> = emptyList(),
  val trashItems: List<TrashItem> = emptyList(),
) {
  fun resolveAccountKey(accountId: String?, methodWire: String): String {
    if (!accountId.isNullOrBlank() && customAccounts.any { it.id == accountId }) {
      return accountId
    }
    return DEFAULT_ACCOUNT_PREFIX + PaymentMethod.fromWire(methodWire).wireValue
  }

  fun accountLabel(accountId: String?, methodWire: String, strings: AppStrings): String {
    if (!accountId.isNullOrBlank()) {
      customAccounts.firstOrNull { it.id == accountId }?.let { return it.name }
    }
    return defaultChannelName(PaymentMethod.fromWire(methodWire), strings)
  }

  fun resolveAccountSummaries(
    strings: AppStrings,
    currencyCode: String = "BDT",
    upToMillis: Long? = null,
  ): List<AccountSummary> {
    val expList = if (upToMillis != null) expenses.filter { it.dateMillis <= upToMillis } else expenses
    val incList = if (upToMillis != null) incomes.filter { it.dateMillis <= upToMillis } else incomes
    val trList = if (upToMillis != null) transfers.filter { it.dateMillis <= upToMillis } else transfers
    val payList = if (upToMillis != null) payables.filter { it.dateMillis <= upToMillis } else payables
    val pmtList = if (upToMillis != null) payablePayments.filter { it.dateMillis <= upToMillis } else payablePayments
    val recList = if (upToMillis != null) receivables.filter { it.dateMillis <= upToMillis } else receivables
    val recPmtList = if (upToMillis != null) receivablePayments.filter { it.dateMillis <= upToMillis } else receivablePayments

    fun netForKey(accountKey: String): Double {
      val inc = incList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      val exp = expList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      val trIn = trList.filter { resolveAccountKey(it.toAccountId, it.toMethod) == accountKey }.sumOf { it.amount }
      val trOut = trList.filter { resolveAccountKey(it.fromAccountId, it.fromMethod) == accountKey }.sumOf { it.amount }
      val borrowed = payList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      val repaid = pmtList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      val lentOut = recList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      val collectedBack = recPmtList.filter { resolveAccountKey(it.accountId, it.paymentMethod) == accountKey }.sumOf { it.amount }
      return inc - exp + trIn - trOut + borrowed - repaid - lentOut + collectedBack
    }

    val defaultChannels = listOf(
      AccountSummary(
        accountId = DEFAULT_ACCOUNT_PREFIX + PaymentMethod.CASH.wireValue,
        name = strings.cash,
        method = PaymentMethod.CASH,
        iconKey = "wallet",
        colorHex = 0xFF0E7C5B,
        openingBalance = 0.0,
        currentBalance = netForKey(DEFAULT_ACCOUNT_PREFIX + PaymentMethod.CASH.wireValue),
        currencyCode = currencyCode,
        isCustom = false,
      ),
      AccountSummary(
        accountId = DEFAULT_ACCOUNT_PREFIX + PaymentMethod.BANK.wireValue,
        name = strings.bank,
        method = PaymentMethod.BANK,
        iconKey = "bank",
        colorHex = 0xFF2563EB,
        openingBalance = 0.0,
        currentBalance = netForKey(DEFAULT_ACCOUNT_PREFIX + PaymentMethod.BANK.wireValue),
        currencyCode = currencyCode,
        isCustom = false,
      ),
      AccountSummary(
        accountId = DEFAULT_ACCOUNT_PREFIX + PaymentMethod.MOBILE.wireValue,
        name = strings.mobileBanking,
        method = PaymentMethod.MOBILE,
        iconKey = "mobile_pay",
        colorHex = 0xFFDB2777,
        openingBalance = 0.0,
        currentBalance = netForKey(DEFAULT_ACCOUNT_PREFIX + PaymentMethod.MOBILE.wireValue),
        currencyCode = currencyCode,
        isCustom = false,
      ),
      AccountSummary(
        accountId = DEFAULT_ACCOUNT_PREFIX + PaymentMethod.CARD.wireValue,
        name = strings.card,
        method = PaymentMethod.CARD,
        iconKey = "card",
        colorHex = 0xFFD97706,
        openingBalance = 0.0,
        currentBalance = netForKey(DEFAULT_ACCOUNT_PREFIX + PaymentMethod.CARD.wireValue),
        currencyCode = currencyCode,
        isCustom = false,
      ),
    )

    val customSummaries = customAccounts.map { acct ->
      val method = PaymentMethod.fromWire(acct.accountType)
      val closing = acct.openingBalance + netForKey(acct.id)
      AccountSummary(
        accountId = acct.id,
        name = acct.name,
        method = method,
        iconKey = acct.iconKey,
        colorHex = acct.colorHex,
        openingBalance = acct.openingBalance,
        currentBalance = closing,
        currencyCode = acct.currencyCode.ifBlank { currencyCode },
        isCustom = true,
        customAccount = acct,
      )
    }

    return defaultChannels + customSummaries
  }

  fun accountBalances(): AccountBalances {
    val defaultChannels = listOf(PaymentMethod.CASH, PaymentMethod.BANK, PaymentMethod.MOBILE, PaymentMethod.CARD)
    fun sumMethod(method: PaymentMethod): Double {
      val defaultKey = DEFAULT_ACCOUNT_PREFIX + method.wireValue
      fun netForKey(k: String): Double {
        val inc = incomes.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        val exp = expenses.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        val trIn = transfers.filter { resolveAccountKey(it.toAccountId, it.toMethod) == k }.sumOf { it.amount }
        val trOut = transfers.filter { resolveAccountKey(it.fromAccountId, it.fromMethod) == k }.sumOf { it.amount }
        val borrowed = payables.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        val repaid = payablePayments.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        val lentOut = receivables.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        val collectedBack = receivablePayments.filter { resolveAccountKey(it.accountId, it.paymentMethod) == k }.sumOf { it.amount }
        return inc - exp + trIn - trOut + borrowed - repaid - lentOut + collectedBack
      }
      val channelNet = netForKey(defaultKey)
      val customSum = customAccounts
        .filter { PaymentMethod.fromWire(it.accountType) == method }
        .sumOf { it.openingBalance + netForKey(it.id) }
      return channelNet + customSum
    }
    val map = defaultChannels.associateWith { sumMethod(it) }
    return AccountBalances(
      cash = map[PaymentMethod.CASH] ?: 0.0,
      bank = map[PaymentMethod.BANK] ?: 0.0,
      mobile = map[PaymentMethod.MOBILE] ?: 0.0,
      card = map[PaymentMethod.CARD] ?: 0.0,
    )
  }

  fun paidAmountForPayable(payable: Payable): Double {
    val matching = payablePayments.filter { it.payableId == payable.id }
    return if (matching.isNotEmpty()) matching.sumOf { it.amount } else payable.paidAmount
  }

  fun totalUnpaidPayables(): Double =
    payables.sumOf { p -> (p.amount - paidAmountForPayable(p)).coerceAtLeast(0.0) }

  fun collectedAmountForReceivable(receivable: Receivable): Double {
    val matching = receivablePayments.filter { it.receivableId == receivable.id }
    return if (matching.isNotEmpty()) matching.sumOf { it.amount } else receivable.collectedAmount
  }

  fun totalUnpaidReceivables(): Double =
    receivables.sumOf { r -> (r.amount - collectedAmountForReceivable(r)).coerceAtLeast(0.0) }

  companion object {
    const val DEFAULT_ACCOUNT_PREFIX = "default_"

    fun defaultChannelName(method: PaymentMethod, strings: AppStrings): String = when (method) {
      PaymentMethod.CASH -> strings.cash
      PaymentMethod.BANK -> strings.bank
      PaymentMethod.MOBILE -> strings.mobileBanking
      PaymentMethod.CARD -> strings.card
    }
  }
}

object HisabAnalytics {

  fun computeMonthlySummary(
    data: DashboardUiData,
    monthKey: String,
    language: AppLanguage,
  ): MonthlySummary {
    val strings = stringsFor(language)
    val categories = CategoryCatalog.mergeCategories(strings, data.customCategories)
    val catMap = categories.associateBy { it.id }

    val monthExpenses = data.expenses.filter { it.monthKey == monthKey }
    val monthIncomes = data.incomes.filter { it.monthKey == monthKey }
    val prevMonthKey = DateFormatUtils.shiftMonthKey(monthKey, -1)
    val prevExpenses = data.expenses.filter { it.monthKey == prevMonthKey }

    val totalSpent = monthExpenses.sumOf { it.amount }
    val totalIncome = monthIncomes.sumOf { it.amount }
    val prevSpent = prevExpenses.sumOf { it.amount }

    val budgetDoc = data.budgets.firstOrNull { it.monthKey == monthKey }
      ?: data.budgets.firstOrNull { it.monthKey == "default" }
    val budgetLimit = budgetDoc?.totalLimit ?: 0.0
    val catLimits = budgetDoc?.categoryLimits.orEmpty()

    val groupedByCat = monthExpenses.groupBy { it.categoryId }
    val breakdowns = groupedByCat.map { (catId, list) ->
      val cat = catMap[catId] ?: CategoryItem(catId, catId, "other", 0xFF0891B2)
      val sum = list.sumOf { it.amount }
      val pct = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
      CategoryBreakdown(
        category = cat,
        totalAmount = sum,
        percentage = pct,
        transactionCount = list.size,
        budgetLimit = catLimits[catId] ?: 0.0,
      )
    }.sortedByDescending { it.totalAmount }

    val dailyTotals = mutableMapOf<Int, Double>()
    val cal = Calendar.getInstance()
    for (exp in monthExpenses) {
      cal.timeInMillis = exp.dateMillis
      val day = cal.get(Calendar.DAY_OF_MONTH)
      dailyTotals[day] = (dailyTotals[day] ?: 0.0) + exp.amount
    }

    val daysCount = DateFormatUtils.daysInMonth(monthKey)
    val activeDays = if (monthKey == DateFormatUtils.currentMonthKey()) {
      Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    } else {
      daysCount
    }
    val dailyAvg = if (totalSpent > 0) totalSpent / activeDays else 0.0

    val sixMonthTotals = (-5..0).map { offset ->
      val mk = DateFormatUtils.shiftMonthKey(monthKey, offset)
      val sum = data.expenses.filter { it.monthKey == mk }.sumOf { it.amount }
      DateFormatUtils.formatShortMonthLabel(mk, language.code) to sum
    }

    return MonthlySummary(
      monthKey = monthKey,
      totalSpent = totalSpent,
      totalIncome = totalIncome,
      netBalance = totalIncome - totalSpent,
      prevMonthSpent = prevSpent,
      transactionCount = monthExpenses.size,
      dailyAverage = dailyAvg,
      topCategory = breakdowns.firstOrNull(),
      budgetLimit = budgetLimit,
      categoryBreakdowns = breakdowns,
      dailyTotals = dailyTotals,
      sixMonthTotals = sixMonthTotals,
    )
  }

  fun computePeriodAnalytics(
    data: DashboardUiData,
    filter: PeriodFilter,
    customMonthKey: String,
    language: AppLanguage,
  ): PeriodAnalyticsSummary {
    val strings = stringsFor(language)
    val (startMillis, endMillis) = DateFormatUtils.periodRangeMillis(filter, customMonthKey)
    val effectiveMonthKey = when (filter) {
      PeriodFilter.THIS_MONTH -> DateFormatUtils.currentMonthKey()
      PeriodFilter.LAST_MONTH -> DateFormatUtils.shiftMonthKey(DateFormatUtils.currentMonthKey(), -1)
      else -> customMonthKey
    }

    val allCats = CategoryCatalog.mergeCategories(strings, data.customCategories, includeIncome = true)
    val catMap = allCats.associateBy { it.id }

    val periodExpenses = data.expenses.filter { it.dateMillis in startMillis..endMillis }
    val periodIncomes = data.incomes.filter { it.dateMillis in startMillis..endMillis }

    val spanMillis = (endMillis - startMillis + 1L).coerceAtLeast(1L)
    val prevStart = startMillis - spanMillis
    val prevEnd = startMillis - 1L
    val prevPeriodSpent = data.expenses
      .filter { it.dateMillis in prevStart..prevEnd }
      .sumOf { it.amount }

    val totalSpent = periodExpenses.sumOf { it.amount }
    val totalIncome = periodIncomes.sumOf { it.amount }
    val netIncome = totalIncome - totalSpent
    val savingsRate = if (totalIncome > 0.0) {
      ((totalIncome - totalSpent) / totalIncome).toFloat().coerceIn(-1f, 1f)
    } else {
      0f
    }

    val budgetDoc = data.budgets.firstOrNull { it.monthKey == effectiveMonthKey }
      ?: data.budgets.firstOrNull { it.monthKey == "default" }
    val budgetLimit = budgetDoc?.totalLimit ?: 0.0
    val catLimits = budgetDoc?.categoryLimits.orEmpty()

    val expenseBreakdowns = periodExpenses
      .groupBy { it.categoryId }
      .map { (catId, list) ->
        val cat = catMap[catId] ?: CategoryItem(catId, catId, "other", 0xFF0891B2)
        val sum = list.sumOf { it.amount }
        CategoryBreakdown(
          category = cat,
          totalAmount = sum,
          percentage = if (totalSpent > 0.0) (sum / totalSpent).toFloat() else 0f,
          transactionCount = list.size,
          budgetLimit = catLimits[catId] ?: 0.0,
        )
      }.sortedByDescending { it.totalAmount }

    val incomeBreakdowns = periodIncomes
      .groupBy { it.categoryId?.ifBlank { "inc_other" } ?: "inc_other" }
      .map { (catId, list) ->
        val cat = catMap[catId] ?: CategoryItem(catId, strings.catOtherIncome, "savings", 0xFF0891B2, flowType = CategoryFlowType.INCOME)
        val sum = list.sumOf { it.amount }
        CategoryBreakdown(
          category = cat,
          totalAmount = sum,
          percentage = if (totalIncome > 0.0) (sum / totalIncome).toFloat() else 0f,
          transactionCount = list.size,
          budgetLimit = 0.0,
        )
      }.sortedByDescending { it.totalAmount }

    val dailyTotals = mutableMapOf<Int, Double>()
    val cal = Calendar.getInstance()
    for (exp in periodExpenses) {
      cal.timeInMillis = exp.dateMillis
      val day = cal.get(Calendar.DAY_OF_MONTH)
      dailyTotals[day] = (dailyTotals[day] ?: 0.0) + exp.amount
    }

    val daysInSpan = ((minOf(endMillis, System.currentTimeMillis()) - startMillis) / 86_400_000L + 1L)
      .coerceIn(1L, 366L)
      .toInt()
    val dailyAverage = if (totalSpent > 0.0) totalSpent / daysInSpan else 0.0

    val anchorMonth = when (filter) {
      PeriodFilter.LAST_MONTH -> DateFormatUtils.shiftMonthKey(DateFormatUtils.currentMonthKey(), -1)
      PeriodFilter.CUSTOM_MONTH -> customMonthKey
      else -> DateFormatUtils.currentMonthKey()
    }
    val incomeVsExpenseMonthly = (-5..0).map { offset ->
      val mk = DateFormatUtils.shiftMonthKey(anchorMonth, offset)
      val mInc = data.incomes.filter { it.monthKey == mk }.sumOf { it.amount }
      val mExp = data.expenses.filter { it.monthKey == mk }.sumOf { it.amount }
      Triple(DateFormatUtils.formatShortMonthLabel(mk, language.code), mInc, mExp)
    }
    val netCashFlowSeries = incomeVsExpenseMonthly.map { (label, inc, exp) ->
      label to (inc - exp)
    }

    val todayExp = data.expenses.filter { DateFormatUtils.isToday(it.dateMillis) }
    val todayInc = data.incomes.filter { DateFormatUtils.isToday(it.dateMillis) }

    return PeriodAnalyticsSummary(
      filter = filter,
      monthKey = effectiveMonthKey,
      startMillis = startMillis,
      endMillis = endMillis,
      totalIncome = totalIncome,
      totalSpent = totalSpent,
      netIncome = netIncome,
      savingsRate = savingsRate,
      prevPeriodSpent = prevPeriodSpent,
      transactionCount = periodExpenses.size + periodIncomes.size,
      dailyAverage = dailyAverage,
      topCategory = expenseBreakdowns.firstOrNull(),
      budgetLimit = budgetLimit,
      expenseBreakdowns = expenseBreakdowns,
      incomeBreakdowns = incomeBreakdowns,
      dailyTotals = dailyTotals,
      incomeVsExpenseMonthly = incomeVsExpenseMonthly,
      netCashFlowSeries = netCashFlowSeries,
      todaySpent = todayExp.sumOf { it.amount },
      todayIncome = todayInc.sumOf { it.amount },
      todayCount = todayExp.size + todayInc.size,
    )
  }

  fun computeAccountLedger(
    data: DashboardUiData,
    selectedAccountKey: String?,
    startMillis: Long,
    endMillis: Long,
    strings: AppStrings,
  ): AccountLedgerResult {
    val allCats = CategoryCatalog.mergeCategories(strings, data.customCategories, includeIncome = true)
      .associateBy { it.id }

    fun matchesAccount(accountId: String?, methodWire: String): Boolean {
      if (selectedAccountKey == null) return true
      return data.resolveAccountKey(accountId, methodWire) == selectedAccountKey
    }

    val baseOpening = when {
      selectedAccountKey == null -> data.customAccounts.sumOf { it.openingBalance }
      else -> data.customAccounts.firstOrNull { it.id == selectedAccountKey }?.openingBalance ?: 0.0
    }

    val preInc = data.incomes.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }
    val preExp = data.expenses.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }
    val preBorrow = data.payables.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }
    val preRepay = data.payablePayments.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }
    val preLend = data.receivables.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }
    val preCollect = data.receivablePayments.filter { it.dateMillis < startMillis && matchesAccount(it.accountId, it.paymentMethod) }.sumOf { it.amount }

    val preTransferNet = if (selectedAccountKey == null) {
      0.0
    } else {
      val inTr = data.transfers.filter {
        it.dateMillis < startMillis && data.resolveAccountKey(it.toAccountId, it.toMethod) == selectedAccountKey
      }.sumOf { it.amount }
      val outTr = data.transfers.filter {
        it.dateMillis < startMillis && data.resolveAccountKey(it.fromAccountId, it.fromMethod) == selectedAccountKey
      }.sumOf { it.amount }
      inTr - outTr
    }

    val openingBalance = baseOpening + preInc - preExp + preBorrow - preRepay - preLend + preCollect + preTransferNet

    data class RawRow(
      val dateMillis: Long,
      val title: String,
      val accountName: String,
      val categoryOrType: String,
      val receipt: Double,
      val payment: Double,
      val entityType: String,
    )

    val rawRows = buildList {
      for (inc in data.incomes) {
        if (inc.dateMillis in startMillis..endMillis && matchesAccount(inc.accountId, inc.paymentMethod)) {
          val catName = inc.categoryId?.let { allCats[it]?.name } ?: strings.entryTypeIncome
          add(
            RawRow(
              dateMillis = inc.dateMillis,
              title = inc.title,
              accountName = data.accountLabel(inc.accountId, inc.paymentMethod, strings),
              categoryOrType = catName,
              receipt = inc.amount,
              payment = 0.0,
              entityType = "INCOME",
            )
          )
        }
      }
      for (exp in data.expenses) {
        if (exp.dateMillis in startMillis..endMillis && matchesAccount(exp.accountId, exp.paymentMethod)) {
          val catName = allCats[exp.categoryId]?.name ?: exp.categoryId
          add(
            RawRow(
              dateMillis = exp.dateMillis,
              title = exp.title,
              accountName = data.accountLabel(exp.accountId, exp.paymentMethod, strings),
              categoryOrType = catName,
              receipt = 0.0,
              payment = exp.amount,
              entityType = "EXPENSE",
            )
          )
        }
      }
      if (selectedAccountKey != null) {
        for (tr in data.transfers) {
          if (tr.dateMillis in startMillis..endMillis) {
            val fromKey = data.resolveAccountKey(tr.fromAccountId, tr.fromMethod)
            val toKey = data.resolveAccountKey(tr.toAccountId, tr.toMethod)
            val fromLbl = data.accountLabel(tr.fromAccountId, tr.fromMethod, strings)
            val toLbl = data.accountLabel(tr.toAccountId, tr.toMethod, strings)
            if (toKey == selectedAccountKey) {
              add(
                RawRow(
                  dateMillis = tr.dateMillis,
                  title = "${strings.entryTypeTransfer}: $fromLbl → $toLbl",
                  accountName = toLbl,
                  categoryOrType = strings.entryTypeTransfer,
                  receipt = tr.amount,
                  payment = 0.0,
                  entityType = "TRANSFER",
                )
              )
            }
            if (fromKey == selectedAccountKey) {
              add(
                RawRow(
                  dateMillis = tr.dateMillis,
                  title = "${strings.entryTypeTransfer}: $fromLbl → $toLbl",
                  accountName = fromLbl,
                  categoryOrType = strings.entryTypeTransfer,
                  receipt = 0.0,
                  payment = tr.amount,
                  entityType = "TRANSFER",
                )
              )
            }
          }
        }
      }
      for (pay in data.payables) {
        if (pay.dateMillis in startMillis..endMillis && matchesAccount(pay.accountId, pay.paymentMethod)) {
          add(
            RawRow(
              dateMillis = pay.dateMillis,
              title = "${strings.entryTypeBorrow}: ${pay.personName}",
              accountName = data.accountLabel(pay.accountId, pay.paymentMethod, strings),
              categoryOrType = strings.entryTypeBorrow,
              receipt = pay.amount,
              payment = 0.0,
              entityType = "PAYABLE",
            )
          )
        }
      }
      for (pmt in data.payablePayments) {
        if (pmt.dateMillis in startMillis..endMillis && matchesAccount(pmt.accountId, pmt.paymentMethod)) {
          add(
            RawRow(
              dateMillis = pmt.dateMillis,
              title = "${strings.entryTypeRepayment}: ${pmt.personName}",
              accountName = data.accountLabel(pmt.accountId, pmt.paymentMethod, strings),
              categoryOrType = strings.entryTypeRepayment,
              receipt = 0.0,
              payment = pmt.amount,
              entityType = "PAYABLE_PAYMENT",
            )
          )
        }
      }
      for (rec in data.receivables) {
        if (rec.dateMillis in startMillis..endMillis && matchesAccount(rec.accountId, rec.paymentMethod)) {
          add(
            RawRow(
              dateMillis = rec.dateMillis,
              title = "${strings.entryTypeLend}: ${rec.personName}",
              accountName = data.accountLabel(rec.accountId, rec.paymentMethod, strings),
              categoryOrType = strings.entryTypeLend,
              receipt = 0.0,
              payment = rec.amount,
              entityType = "RECEIVABLE",
            )
          )
        }
      }
      for (pmt in data.receivablePayments) {
        if (pmt.dateMillis in startMillis..endMillis && matchesAccount(pmt.accountId, pmt.paymentMethod)) {
          add(
            RawRow(
              dateMillis = pmt.dateMillis,
              title = "${strings.entryTypeCollection}: ${pmt.personName}",
              accountName = data.accountLabel(pmt.accountId, pmt.paymentMethod, strings),
              categoryOrType = strings.entryTypeCollection,
              receipt = pmt.amount,
              payment = 0.0,
              entityType = "RECEIVABLE_PAYMENT",
            )
          )
        }
      }
    }.sortedBy { it.dateMillis }

    var running = openingBalance
    var totalReceipts = 0.0
    var totalPayments = 0.0
    val entries = rawRows.map { row ->
      totalReceipts += row.receipt
      totalPayments += row.payment
      running += row.receipt - row.payment
      LedgerEntry(
        dateMillis = row.dateMillis,
        title = row.title,
        accountName = row.accountName,
        categoryOrType = row.categoryOrType,
        receiptAmount = row.receipt,
        paymentAmount = row.payment,
        runningBalance = running,
        entityType = row.entityType,
      )
    }

    return AccountLedgerResult(
      openingBalance = openingBalance,
      totalReceipts = totalReceipts,
      totalPayments = totalPayments,
      netChange = totalReceipts - totalPayments,
      closingBalance = running,
      entries = entries,
    )
  }

  fun computeTrialBalance(
    data: DashboardUiData,
    startMillis: Long,
    endMillis: Long,
    strings: AppStrings,
    currencyCode: String = "BDT",
  ): TrialBalanceResult {
    val allCats = CategoryCatalog.mergeCategories(strings, data.customCategories, includeIncome = true)
      .associateBy { it.id }

    // 1. Assets: Cash/Bank/Mobile/Card balances as of endMillis + Net Receivables as of endMillis
    val accountSummaries = data.resolveAccountSummaries(strings, currencyCode, upToMillis = endMillis)
    val assetLines = mutableListOf<TrialBalanceLine>()
    val liabilityLines = mutableListOf<TrialBalanceLine>()

    for (acct in accountSummaries) {
      if (abs(acct.currentBalance) >= 0.005) {
        if (acct.currentBalance > 0.0) {
          assetLines.add(TrialBalanceLine(acct.name, acct.currentBalance, 0.0))
        } else {
          liabilityLines.add(TrialBalanceLine("${acct.name} (Overdraft)", 0.0, -acct.currentBalance))
        }
      }
    }

    val totalLent = data.receivables.filter { it.dateMillis <= endMillis }.sumOf { it.amount }
    val totalCollected = data.receivablePayments.filter { it.dateMillis <= endMillis }.sumOf { it.amount }
    val netReceivables = totalLent - totalCollected
    if (abs(netReceivables) >= 0.005) {
      if (netReceivables > 0.0) {
        assetLines.add(TrialBalanceLine(strings.receivablesTab, netReceivables, 0.0))
      } else {
        liabilityLines.add(TrialBalanceLine(strings.receivablesTab, 0.0, -netReceivables))
      }
    }

    // 2. Liabilities & Opening Capital / Pre-Period Retained Equity
    val totalBorrowed = data.payables.filter { it.dateMillis <= endMillis }.sumOf { it.amount }
    val totalRepaid = data.payablePayments.filter { it.dateMillis <= endMillis }.sumOf { it.amount }
    val netPayables = totalBorrowed - totalRepaid
    if (abs(netPayables) >= 0.005) {
      if (netPayables > 0.0) {
        liabilityLines.add(TrialBalanceLine(strings.payablesTab, 0.0, netPayables))
      } else {
        assetLines.add(TrialBalanceLine(strings.payablesTab, -netPayables, 0.0))
      }
    }

    val openingCapital = data.customAccounts.sumOf { it.openingBalance } +
      data.incomes.filter { it.dateMillis < startMillis }.sumOf { it.amount } -
      data.expenses.filter { it.dateMillis < startMillis }.sumOf { it.amount }
    if (abs(openingCapital) >= 0.005) {
      if (openingCapital > 0.0) {
        liabilityLines.add(TrialBalanceLine(strings.openingBalanceLabel, 0.0, openingCapital))
      } else {
        assetLines.add(TrialBalanceLine(strings.openingBalanceLabel, -openingCapital, 0.0))
      }
    }

    // 3. Revenue (Incomes in period) -> Credit
    val revenueLines = data.incomes
      .filter { it.dateMillis in startMillis..endMillis }
      .groupBy { it.categoryId?.ifBlank { "inc_other" } ?: "inc_other" }
      .map { (catId, list) ->
        val name = allCats[catId]?.name ?: strings.catOtherIncome
        TrialBalanceLine(name, 0.0, list.sumOf { it.amount })
      }
      .filter { it.creditAmount >= 0.005 }
      .sortedByDescending { it.creditAmount }

    // 4. Operating Expenses (Expenses in period) -> Debit
    val expenseLines = data.expenses
      .filter { it.dateMillis in startMillis..endMillis }
      .groupBy { it.categoryId }
      .map { (catId, list) ->
        val name = allCats[catId]?.name ?: catId
        TrialBalanceLine(name, list.sumOf { it.amount }, 0.0)
      }
      .filter { it.debitAmount >= 0.005 }
      .sortedByDescending { it.debitAmount }

    val groups = listOf(
      TrialBalanceGroup(
        groupName = strings.assetsSection,
        lines = assetLines,
        totalDebit = assetLines.sumOf { it.debitAmount },
        totalCredit = assetLines.sumOf { it.creditAmount },
      ),
      TrialBalanceGroup(
        groupName = strings.liabilitiesSection,
        lines = liabilityLines,
        totalDebit = liabilityLines.sumOf { it.debitAmount },
        totalCredit = liabilityLines.sumOf { it.creditAmount },
      ),
      TrialBalanceGroup(
        groupName = strings.revenueSection,
        lines = revenueLines,
        totalDebit = revenueLines.sumOf { it.debitAmount },
        totalCredit = revenueLines.sumOf { it.creditAmount },
      ),
      TrialBalanceGroup(
        groupName = strings.operatingExpensesSection,
        lines = expenseLines,
        totalDebit = expenseLines.sumOf { it.debitAmount },
        totalCredit = expenseLines.sumOf { it.creditAmount },
      ),
    ).filter { it.lines.isNotEmpty() }

    val totalDebit = groups.sumOf { it.totalDebit }
    val totalCredit = groups.sumOf { it.totalCredit }
    val isBalanced = abs(totalDebit - totalCredit) <= 0.01

    return TrialBalanceResult(
      groups = groups,
      totalDebit = totalDebit,
      totalCredit = totalCredit,
      isBalanced = isBalanced,
    )
  }
}
