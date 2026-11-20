package com.example.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String) {
  BENGALI("bn", "বাংলা"),
  ENGLISH("en", "English"),
  HINDI("hi", "हिन्दी");

  companion object {
    fun fromCode(code: String?): AppLanguage =
      entries.firstOrNull { it.code == code } ?: BENGALI
  }
}

@Immutable
interface AppStrings {
  val appName: String
  val tagline: String
  val navExpenses: String
  val navReports: String
  val navBudgets: String
  val navSettings: String
  val signInTitle: String
  val signInSubtitle: String
  val signInWithGoogle: String
  val adFreeBadge: String
  val featureOfflineTitle: String
  val featureOfflineDesc: String
  val featureCloudTitle: String
  val featureCloudDesc: String
  val featureDriveTitle: String
  val featureDriveDesc: String
  val onlineSynced: String
  val offlineModeActive: String
  val offlineCached: String
  val syncingPending: String
  val thisMonthSpend: String
  val budgetRemaining: String
  val budgetExceededBy: String
  val setBudgetHint: String
  val dailyAllowance: String
  val today: String
  val yesterday: String
  val allCategories: String
  val searchExpenses: String
  val addExpense: String
  val editExpense: String
  val expenseTitleLabel: String
  val amountLabel: String
  val categoryLabel: String
  val paymentMethodLabel: String
  val dateLabel: String
  val noteOptionalLabel: String
  val save: String
  val cancel: String
  val delete: String
  val confirmDeleteTitle: String
  val confirmDeleteMessage: String
  val emptyExpensesTitle: String
  val emptyExpensesBody: String
  val addCustomCategory: String
  val customCategoryName: String
  val chooseColor: String
  val chooseIcon: String
  val cash: String
  val card: String
  val mobileBanking: String
  val bank: String
  val monthlyReportTitle: String
  val totalSpent: String
  val dailyAverage: String
  val transactionsCount: String
  val topCategory: String
  val vsPrevMonth: String
  val categoryChartTitle: String
  val dailyTrendTitle: String
  val sixMonthChartTitle: String
  val categoryBreakdown: String
  val tapBarHint: String
  val noDataForMonth: String
  val budgetSettingsTitle: String
  val monthlyBudgetLimit: String
  val categoryBudgetsTitle: String
  val saveBudget: String
  val budgetSaved: String
  val spentOf: String
  val reminderSettingsTitle: String
  val dailyReminderLabel: String
  val dailyReminderDesc: String
  val reminderTimeLabel: String
  val budgetAlertsLabel: String
  val budgetAlertsDesc: String
  val notificationPermNeeded: String
  val grantPermission: String
  val settingsTitle: String
  val accountSection: String
  val signOut: String
  val syncSection: String
  val cloudSyncToggle: String
  val cloudSyncDesc: String
  val driveBackupSection: String
  val backUpToDrive: String
  val restoreFromDrive: String
  val lastDriveBackup: String
  val neverBackedUp: String
  val driveBackupSuccess: String
  val driveRestoreSuccess: String
  val chooseBackupTitle: String
  val noDriveBackups: String
  val localExportSection: String
  val exportCsv: String
  val exportJson: String
  val importJson: String
  val exportSuccess: String
  val importSuccess: String
  val securitySection: String
  val pinLockToggle: String
  val pinLockDesc: String
  val changePin: String
  val lockOnBackground: String
  val enterPinTitle: String
  val createPinTitle: String
  val confirmPinTitle: String
  val pinMismatch: String
  val wrongPin: String
  val pinSaved: String
  val appearanceSection: String
  val themeModeLabel: String
  val themeSystem: String
  val themeLight: String
  val themeDark: String
  val themePaletteLabel: String
  val languageCurrencySection: String
  val languageLabel: String
  val currencyLabel: String
  val customCategoriesSection: String
  val adFreeNotice: String
  val invalidInputError: String
  val catFood: String
  val catTransport: String
  val catGroceries: String
  val catUtilities: String
  val catShopping: String
  val catHealth: String
  val catEducation: String
  val catEntertainment: String
  val catFamily: String
  val catOther: String
  val budgetAlert80Title: String
  val budgetAlert100Title: String
  val reminderNotificationTitle: String
  val reminderNotificationBody: String
  val themeEmerald: String
  val themeIndigo: String
  val themeTerracotta: String
  val themeOcean: String
  val themePlum: String
  val remainingBalance: String
  val addedBalance: String
  val addBalance: String
  val editBalance: String
  val balanceTitleLabel: String
  val balanceFilterChip: String
  val expenseTypeTab: String
  val balanceTypeTab: String
  val receiveMethodLabel: String
  val confirmDeleteBalanceTitle: String
  val confirmDeleteBalanceMessage: String
  val overallWalletBalance: String
  val entryTypeExpense: String
  val entryTypeIncome: String
  val navWallets: String
  val transferTypeTab: String
  val borrowTypeTab: String
  val addTransfer: String
  val editTransfer: String
  val fromAccountLabel: String
  val toAccountLabel: String
  val sameAccountTransferError: String
  val addBorrow: String
  val editBorrow: String
  val lenderNameLabel: String
  val borrowAutoPayableHint: String
  val transferFilterChip: String
  val payableFilterChip: String
  val walletsScreenTitle: String
  val accountBalancesTitle: String
  val totalPayablesLabel: String
  val unpaidPayablesTitle: String
  val settledPayablesTitle: String
  val noPayablesEmpty: String
  val repayButton: String
  val repayPayableTitle: String
  val paidFromAccountLabel: String
  val paidAmountLabel: String
  val dueAmountLabel: String
  val settledBadge: String
  val repaymentHistoryTitle: String
  val transferHistoryTitle: String
  val quickTransferAction: String
  val quickBorrowAction: String
  val confirmDeleteTransferTitle: String
  val confirmDeleteTransferMessage: String
  val confirmDeletePayableTitle: String
  val confirmDeletePayableMessage: String
  val confirmDeleteRepaymentTitle: String
  val confirmDeleteRepaymentMessage: String
  val entryTypeTransfer: String
  val entryTypeBorrow: String
  val entryTypeRepayment: String
  val walletCashLabel: String
  val lendTypeTab: String
  val addLend: String
  val editLend: String
  val borrowerNameLabel: String
  val lendAutoReceivableHint: String
  val lendFromAccountLabel: String
  val receivableFilterChip: String
  val totalReceivablesLabel: String
  val unpaidReceivablesTitle: String
  val settledReceivablesTitle: String
  val noReceivablesEmpty: String
  val collectButton: String
  val collectReceivableTitle: String
  val collectedToAccountLabel: String
  val collectedAmountLabel: String
  val collectionHistoryTitle: String
  val quickLendAction: String
  val confirmDeleteReceivableTitle: String
  val confirmDeleteReceivableMessage: String
  val confirmDeleteCollectionTitle: String
  val confirmDeleteCollectionMessage: String
  val entryTypeLend: String
  val entryTypeCollection: String
  val payablesTab: String
  val receivablesTab: String
  val marketListTitle: String
  val marketCardTitle: String
  val marketCardSubtitle: String
  val marketNewListButton: String
  val marketListNameLabel: String
  val shareWithEmailLabel: String
  val shareWithEmailHint: String
  val sharedByLabel: String
  val sharedWithLabel: String
  val noMarketListsTitle: String
  val noMarketListsBody: String
  val itemNameLabel: String
  val itemQtyLabel: String
  val itemPriceOptionalLabel: String
  val addItemButton: String
  val boughtLabel: String
  val remainingItemsLabel: String
  val estimatedTotalLabel: String
  val logAsExpenseButton: String
  val markCompletedButton: String
  val reopenListButton: String
  val shareListTitle: String
  val shareListButton: String
  val shareViaTextButton: String
  val marketExpenseSaved: String
  val confirmDeleteMarketListTitle: String
  val confirmDeleteMarketListMessage: String
  val editItemTitle: String
  val completedBadge: String
  val activeBadge: String
  val sharedListSavedMsg: String
  val splitByCategoryOption: String
  val singleCategoryOption: String
  val marketSplitExpenseSaved: String
  val itemCategoryLabel: String
  val navDashboard: String
  val navTransactions: String
  val navExpensesList: String
  val navIncomesList: String
  val navAccounts: String
  val navCategories: String
  val navLoansDebts: String
  val navLedger: String
  val navTrialBalance: String
  val navTrash: String
  val drawerSectionMain: String
  val drawerSectionLoans: String
  val drawerSectionReports: String
  val drawerSectionSettings: String
  val periodThisMonth: String
  val periodLastMonth: String
  val periodThisQuarter: String
  val periodThisYear: String
  val periodCustomMonth: String
  val todaysActivityTitle: String
  val netCashFlowTitle: String
  val surplusLabel: String
  val deficitLabel: String
  val incomeVsExpenseTitle: String
  val barChartLabel: String
  val lineChartLabel: String
  val recentTransactionsTitle: String
  val viewAllButton: String
  val netIncomeLabel: String
  val totalIncomeLabel: String
  val savingsRateLabel: String
  val topCategoriesTitle: String
  val catSalary: String
  val catBusiness: String
  val catInvestment: String
  val catOtherIncome: String
  val categoryTypeLabel: String
  val expenseCategoryType: String
  val incomeCategoryType: String
  val customBadge: String
  val allFilterTab: String
  val singleModeTab: String
  val multipleModeTab: String
  val savedBadgeCount: String
  val addAnotherHint: String
  val timeLabel: String
  val dueDateOptionalLabel: String
  val clearDueDate: String
  val titleOptionalHint: String
  val selectAccountLabel: String
  val accountsLabel: String
  val addAccountButton: String
  val editAccountTitle: String
  val accountNameLabel: String
  val accountTypeLabel: String
  val openingBalanceLabel: String
  val presetsLabel: String
  val confirmDeleteAccountTitle: String
  val confirmDeleteAccountMessage: String
  val searchIconsHint: String
  val customHexHint: String
  val sortLabel: String
  val sortNewest: String
  val sortOldest: String
  val sortAmountHigh: String
  val sortAmountLow: String
  val selectedCountLabel: String
  val bulkDeleteButton: String
  val loadMoreButton: String
  val overdueBadge: String
  val overdueCountLabel: String
  val partialBadge: String
  val unpaidBadge: String
  val dueDateLabel: String
  val allAccountsOption: String
  val receiptsLabel: String
  val paymentsLabel: String
  val netChangeLabel: String
  val closingBalanceLabel: String
  val runningBalanceLabel: String
  val exportLedgerCsv: String
  val exportTrialBalanceCsv: String
  val balancedBadge: String
  val unbalancedBadge: String
  val debitLabel: String
  val creditLabel: String
  val assetsSection: String
  val liabilitiesSection: String
  val revenueSection: String
  val operatingExpensesSection: String
  val trashEmptyTitle: String
  val trashEmptyBody: String
  val restoreButton: String
  val deleteForeverButton: String
  val emptyTrashButton: String
  val daysRemainingSuffix: String
  val restoredMessage: String
  val movedToTrashMessage: String
  val displayPreferencesSection: String
  val showNotesInListLabel: String
  val use24HourTimeLabel: String
  val itemsPerPageLabel: String
  val allItemsLabel: String
  val blurBalancesLabel: String
  val hapticFeedbackLabel: String
  val dangerZoneSection: String
  val clearAllDataButton: String
  val confirmClearAllTitle: String
  val confirmClearAllMessage: String
  val allDataClearedMessage: String
}

object BengaliStrings : AppStrings {
  override val appName = "হিসাব"
  override val tagline = "আপনার দৈনন্দিন খরচের সহজ ও নিরাপদ সঙ্গী"
  override val navExpenses = "খরচ"
  override val navReports = "রিপোর্ট"
  override val navBudgets = "বাজেট"
  override val navSettings = "সেটিংস"
  override val signInTitle = "হিসাব-এ স্বাগতম"
  override val signInSubtitle = "বিজ্ঞাপন-মুক্ত দৈনন্দিন খরচের হিসাব, অফলাইন ব্যবহার, ক্লাউড সিঙ্ক এবং গুগল ড্রাইভ ব্যাকআপ একসাথে।"
  override val signInWithGoogle = "Google দিয়ে সাইন ইন করুন"
  override val adFreeBadge = "১০০% বিজ্ঞাপন মুক্ত"
  override val featureOfflineTitle = "অফলাইন মোড"
  override val featureOfflineDesc = "ইন্টারনেট ছাড়াই খরচ লিখুন ও দেখুন"
  override val featureCloudTitle = "ক্লাউড সিঙ্ক"
  override val featureCloudDesc = "সব ডিভাইসে স্বয়ংক্রিয়ভাবে ডাটা নিরাপদ থাকবে"
  override val featureDriveTitle = "গুগল ড্রাইভ ব্যাকআপ"
  override val featureDriveDesc = "এক ক্লিকে ড্রাইভে ব্যাকআপ ও রিস্টোর করুন"
  override val onlineSynced = "অনলাইন • সিঙ্ক সম্পন্ন"
  override val offlineModeActive = "অফলাইন মোড চালু"
  override val offlineCached = "অফলাইন • লোকাল সেভ"
  override val syncingPending = "সিঙ্ক হচ্ছে..."
  override val thisMonthSpend = "এই মাসের মোট খরচ"
  override val budgetRemaining = "বাজেট বাকি আছে"
  override val budgetExceededBy = "বাজেট অতিক্রম করেছে"
  override val setBudgetHint = "মাসিক বাজেট সেট করুন"
  override val dailyAllowance = "দৈনিক গড় বরাদ্দ"
  override val today = "আজ"
  override val yesterday = "গতকাল"
  override val allCategories = "সব বিভাগ"
  override val searchExpenses = "খরচ খুঁজুন..."
  override val addExpense = "নতুন খরচ যোগ করুন"
  override val editExpense = "খরচ সম্পাদনা করুন"
  override val expenseTitleLabel = "খরচের বিবরণ (যেমন: দুপুরের খাবার)"
  override val amountLabel = "পরিমাণ"
  override val categoryLabel = "বিভাগ নির্বাচন করুন"
  override val paymentMethodLabel = "পেমেন্ট মাধ্যম"
  override val dateLabel = "তারিখ"
  override val noteOptionalLabel = "নোট (ঐচ্ছিক)"
  override val save = "সংরক্ষণ করুন"
  override val cancel = "বাতিল"
  override val delete = "মুছে ফেলুন"
  override val confirmDeleteTitle = "খরচটি মুছে ফেলবেন?"
  override val confirmDeleteMessage = "এই খরচের রেকর্ডটি স্থায়ীভাবে মুছে যাবে।"
  override val emptyExpensesTitle = "এখনও কোনো খরচ লেখা হয়নি"
  override val emptyExpensesBody = "নিচের '+' বোতামে ট্যাপ করে আপনার প্রথম দৈনন্দিন খরচ যোগ করুন।"
  override val addCustomCategory = "নতুন বিভাগ"
  override val customCategoryName = "বিভাগের নাম"
  override val chooseColor = "রঙ বাছাই করুন"
  override val chooseIcon = "আইকন বাছাই করুন"
  override val cash = "নগদ"
  override val card = "কার্ড"
  override val mobileBanking = "মোবাইল ব্যাংকিং"
  override val bank = "ব্যাংক"
  override val monthlyReportTitle = "মাসিক রিপোর্ট"
  override val totalSpent = "মোট খরচ"
  override val dailyAverage = "দৈনিক গড়"
  override val transactionsCount = "মোট লেনদেন"
  override val topCategory = "সর্বোচ্চ বিভাগ"
  override val vsPrevMonth = "আগের মাসের তুলনায়"
  override val categoryChartTitle = "বিভাগ অনুযায়ী খরচের চিত্র"
  override val dailyTrendTitle = "দৈনিক খরচের গ্রাফ"
  override val sixMonthChartTitle = "গত ৬ মাসের তুলনামূলক চিত্র"
  override val categoryBreakdown = "বিভাগভিত্তিক বিস্তারিত"
  override val tapBarHint = "বিস্তারিত দেখতে যেকোনো বার বা অংশে ট্যাপ করুন"
  override val noDataForMonth = "এই মাসে কোনো খরচের ডাটা নেই"
  override val budgetSettingsTitle = "বাজেট ও রিমাইন্ডার"
  override val monthlyBudgetLimit = "মাসিক মোট বাজেট"
  override val categoryBudgetsTitle = "বিভাগ অনুযায়ী বাজেট"
  override val saveBudget = "বাজেট সংরক্ষণ করুন"
  override val budgetSaved = "বাজেট সফলভাবে সংরক্ষিত হয়েছে"
  override val spentOf = "খরচ হয়েছে /"
  override val reminderSettingsTitle = "নোটিফিকেশন রিমাইন্ডার"
  override val dailyReminderLabel = "দৈনিক খরচ লেখার রিমাইন্ডার"
  override val dailyReminderDesc = "প্রতিদিন নির্দিষ্ট সময়ে আপনাকে মনে করিয়ে দেবে"
  override val reminderTimeLabel = "রিমাইন্ডার সময়"
  override val budgetAlertsLabel = "বাজেট সতর্কতা নোটিফিকেশন"
  override val budgetAlertsDesc = "বাজেটের ৮০% বা ১০০% পার হলে সতর্ক করবে"
  override val notificationPermNeeded = "রিমাইন্ডার পেতে নোটিফিকেশন অনুমতি দিন"
  override val grantPermission = "অনুমতি দিন"
  override val settingsTitle = "সেটিংস ও ব্যাকআপ"
  override val accountSection = "অ্যাকাউন্ট"
  override val signOut = "সাইন আউট"
  override val syncSection = "ক্লাউড সিঙ্ক ও অফলাইন মোড"
  override val cloudSyncToggle = "অটো ক্লাউড সিঙ্ক"
  override val cloudSyncDesc = "বন্ধ করলে অ্যাপটি সম্পূর্ণ অফলাইন মোডে চলবে"
  override val driveBackupSection = "গুগল ড্রাইভ ব্যাকআপ"
  override val backUpToDrive = "গুগল ড্রাইভে ব্যাকআপ নিন"
  override val restoreFromDrive = "গুগল ড্রাইভ থেকে রিস্টোর করুন"
  override val lastDriveBackup = "সর্বশেষ ড্রাইভ ব্যাকআপ"
  override val neverBackedUp = "এখনও ব্যাকআপ নেওয়া হয়নি"
  override val driveBackupSuccess = "গুগল ড্রাইভে ব্যাকআপ সফল হয়েছে!"
  override val driveRestoreSuccess = "গুগল ড্রাইভ থেকে ডাটা রিস্টোর হয়েছে!"
  override val chooseBackupTitle = "রিস্টোর করার জন্য ব্যাকআপ বেছে নিন"
  override val noDriveBackups = "গুগল ড্রাইভে কোনো ব্যাকআপ পাওয়া যায়নি"
  override val localExportSection = "ডাটা এক্সপোর্ট ও লোকাল ব্যাকআপ"
  override val exportCsv = "CSV এক্সপোর্ট (Excel)"
  override val exportJson = "JSON ব্যাকআপ সেভ করুন"
  override val importJson = "JSON ব্যাকআপ ইমপোর্ট করুন"
  override val exportSuccess = "ফাইল সফলভাবে এক্সপোর্ট হয়েছে"
  override val importSuccess = "ব্যাকআপ ডাটা সফলভাবে ইমপোর্ট হয়েছে"
  override val securitySection = "পাসওয়ার্ড / পিন সুরক্ষা"
  override val pinLockToggle = "অ্যাপ পিন লক চালু করুন"
  override val pinLockDesc = "অ্যাপ খোলার সময় ৪-সংখ্যার পিন চাইবে"
  override val changePin = "পিন পরিবর্তন করুন"
  override val lockOnBackground = "ব্যাকগ্রাউন্ডে গেলে লক করুন"
  override val enterPinTitle = "অ্যাপ আনলক করতে পিন দিন"
  override val createPinTitle = "নতুন ৪-সংখ্যার পিন সেট করুন"
  override val confirmPinTitle = "পিনটি পুনরায় নিশ্চিত করুন"
  override val pinMismatch = "পিন মেলেনি, আবার চেষ্টা করুন"
  override val wrongPin = "ভুল পিন! আবার চেষ্টা করুন"
  override val pinSaved = "পিন সুরক্ষা সক্রিয় হয়েছে"
  override val appearanceSection = "থিম ও ডার্ক মোড"
  override val themeModeLabel = "ডিসপ্লে মোড"
  override val themeSystem = "সিস্টেম"
  override val themeLight = "লাইট"
  override val themeDark = "ডার্ক"
  override val themePaletteLabel = "কাস্টম রঙের থিম"
  override val languageCurrencySection = "ভাষা ও মুদ্রা"
  override val languageLabel = "অ্যাপের ভাষা"
  override val currencyLabel = "মুদ্রা (Currency)"
  override val customCategoriesSection = "কাস্টম বিভাগসমূহ"
  override val adFreeNotice = "হিসাব সম্পূর্ণ বিজ্ঞাপন-মুক্ত এবং আপনার ডাটা শুধুমাত্র আপনার নিয়ন্ত্রণে থাকে।"
  override val invalidInputError = "অনুগ্রহ করে সঠিক বিবরণ ও ০-এর বেশি পরিমাণ লিখুন"
  override val catFood = "খাবার"
  override val catTransport = "যাতায়াত"
  override val catGroceries = "বাজার"
  override val catUtilities = "বিল ও ইউটিলিটি"
  override val catShopping = "কেনাকাটা"
  override val catHealth = "স্বাস্থ্য"
  override val catEducation = "শিক্ষা"
  override val catEntertainment = "বিনোদন"
  override val catFamily = "পরিবার ও উপহার"
  override val catOther = "অন্যান্য"
  override val budgetAlert80Title = "বাজেট সতর্কতা (৮০%+)"
  override val budgetAlert100Title = "বাজেট সীমা অতিক্রম!"
  override val reminderNotificationTitle = "আজকের খরচের হিসাব লিখেছেন?"
  override val reminderNotificationBody = "হিসাব অ্যাপে আজকের খরচগুলো যোগ করতে ভুলবেন না।"
  override val themeEmerald = "এমেরাল্ড"
  override val themeIndigo = "ইন্ডিগো"
  override val themeTerracotta = "টেরাকোটা"
  override val themeOcean = "ওশান"
  override val themePlum = "প্লাম"
  override val remainingBalance = "অবশিষ্ট ব্যালেন্স"
  override val addedBalance = "মোট ব্যালেন্স যোগ"
  override val addBalance = "ব্যালেন্স যোগ করুন"
  override val editBalance = "ব্যালেন্স সম্পাদনা করুন"
  override val balanceTitleLabel = "ব্যালেন্সের উৎস (যেমন: বেতন, ক্যাশ জমা)"
  override val balanceFilterChip = "ব্যালেন্স (+)"
  override val expenseTypeTab = "খরচ (-)"
  override val balanceTypeTab = "ব্যালেন্স (+)"
  override val receiveMethodLabel = "জমার মাধ্যম"
  override val confirmDeleteBalanceTitle = "ব্যালেন্স এন্ট্রি মুছে ফেলবেন?"
  override val confirmDeleteBalanceMessage = "এই ব্যালেন্স জমার রেকর্ডটি স্থায়ীভাবে মুছে যাবে।"
  override val overallWalletBalance = "সর্বমোট ওয়ালেট ব্যালেন্স"
  override val entryTypeExpense = "খরচ"
  override val entryTypeIncome = "ব্যালেন্স"
  override val navWallets = "ওয়ালেট"
  override val transferTypeTab = "ট্রান্সফার"
  override val borrowTypeTab = "ধার (+)"
  override val addTransfer = "অ্যাকাউন্ট ট্রান্সফার"
  override val editTransfer = "ট্রান্সফার সম্পাদনা"
  override val fromAccountLabel = "যে অ্যাকাউন্ট থেকে (From)"
  override val toAccountLabel = "যে অ্যাকাউন্টে যাবে (To)"
  override val sameAccountTransferError = "প্রেরক ও প্রাপক অ্যাকাউন্ট আলাদা হতে হবে"
  override val addBorrow = "ধার গ্রহণ (Payable)"
  override val editBorrow = "ধারের হিসাব সম্পাদনা"
  override val lenderNameLabel = "যার থেকে ধার নিলেন (নাম)"
  override val borrowAutoPayableHint = "এই টাকা নির্বাচিত অ্যাকাউন্টে যোগ হবে এবং স্বয়ংক্রিয়ভাবে পাওনাদার (Payable) তৈরি হবে।"
  override val transferFilterChip = "ট্রান্সফার (⇄)"
  override val payableFilterChip = "ধার / দেনা"
  override val walletsScreenTitle = "ওয়ালেট ও দেনা"
  override val accountBalancesTitle = "অ্যাকাউন্ট অনুযায়ী ব্যালেন্স"
  override val totalPayablesLabel = "মোট দেনা (Payable)"
  override val unpaidPayablesTitle = "বাকি দেনা (Payables)"
  override val settledPayablesTitle = "পরিশোধিত দেনা"
  override val noPayablesEmpty = "কোনো বাকি দেনা নেই"
  override val repayButton = "শোধ করুন"
  override val repayPayableTitle = "দেনা পরিশোধ করুন"
  override val paidFromAccountLabel = "যে অ্যাকাউন্ট থেকে পরিশোধ"
  override val paidAmountLabel = "পরিশোধিত"
  override val dueAmountLabel = "বাকি আছে"
  override val settledBadge = "পরিশোধিত"
  override val repaymentHistoryTitle = "পরিশোধের ইতিহাস"
  override val transferHistoryTitle = "অ্যাকাউন্ট ট্রান্সফারসমূহ"
  override val quickTransferAction = "ট্রান্সফার"
  override val quickBorrowAction = "ধার (+)"
  override val confirmDeleteTransferTitle = "ট্রান্সফার মুছে ফেলবেন?"
  override val confirmDeleteTransferMessage = "এই ট্রান্সফার রেকর্ডটি স্থায়ীভাবে মুছে যাবে এবং অ্যাকাউন্ট ব্যালেন্স আপডেট হবে।"
  override val confirmDeletePayableTitle = "দেনার রেকর্ড মুছে ফেলবেন?"
  override val confirmDeletePayableMessage = "এই ধারের রেকর্ড ও এর সব পরিশোধের তথ্য স্থায়ীভাবে মুছে যাবে।"
  override val confirmDeleteRepaymentTitle = "পরিশোধ মুছে ফেলবেন?"
  override val confirmDeleteRepaymentMessage = "এই পরিশোধ মুছে ফেললে দেনার বাকি পরিমাণ আবার বেড়ে যাবে।"
  override val entryTypeTransfer = "ট্রান্সফার"
  override val entryTypeBorrow = "ধার (Payable)"
  override val entryTypeRepayment = "দেনা শোধ"
  override val walletCashLabel = "নগদ (ওয়ালেট)"
  override val lendTypeTab = "ধার দেওয়া"
  override val addLend = "ধার দেওয়া (পাওনা)"
  override val editLend = "পাওনা সম্পাদনা"
  override val borrowerNameLabel = "যাকে ধার দিলেন (নাম)"
  override val lendAutoReceivableHint = "এই টাকা নির্বাচিত অ্যাকাউন্ট থেকে কমবে এবং স্বয়ংক্রিয়ভাবে আপনার পাওনা (Receivable) হিসেবে যুক্ত হবে।"
  override val lendFromAccountLabel = "যে অ্যাকাউন্ট থেকে দিলেন"
  override val receivableFilterChip = "পাওনা (ধার দেওয়া)"
  override val totalReceivablesLabel = "মোট পাওনা (Receivable)"
  override val unpaidReceivablesTitle = "বাকি পাওনা (Receivables)"
  override val settledReceivablesTitle = "আদায়কৃত পাওনা"
  override val noReceivablesEmpty = "কোনো বাকি পাওনা নেই"
  override val collectButton = "আদায় করুন"
  override val collectReceivableTitle = "পাওনা আদায় করুন"
  override val collectedToAccountLabel = "যে অ্যাকাউন্টে পেলেন"
  override val collectedAmountLabel = "আদায় হয়েছে"
  override val collectionHistoryTitle = "আদায়ের ইতিহাস"
  override val quickLendAction = "ধার দেওয়া (-)"
  override val confirmDeleteReceivableTitle = "পাওনার রেকর্ড মুছে ফেলবেন?"
  override val confirmDeleteReceivableMessage = "এই পাওনা ও এর সব আদায়ের তথ্য স্থায়ীভাবে মুছে যাবে।"
  override val confirmDeleteCollectionTitle = "আদায় মুছে ফেলবেন?"
  override val confirmDeleteCollectionMessage = "এই আদায় মুছে ফেললে পাওনার বাকি পরিমাণ আবার বেড়ে যাবে।"
  override val entryTypeLend = "ধার দেওয়া (পাওনা)"
  override val entryTypeCollection = "পাওনা আদায়"
  override val payablesTab = "দেনা (নিয়েছি)"
  override val receivablesTab = "পাওনা (দিয়েছি)"
  override val marketListTitle = "বাজার লিস্ট"
  override val marketCardTitle = "বাজার লিস্ট ও শেয়ার"
  override val marketCardSubtitle = "নতুন বাজার লিস্ট বানান বা ইমেইলে পাঠান"
  override val marketNewListButton = "নতুন লিস্ট"
  override val marketListNameLabel = "লিস্টের নাম (যেমন: আজকের বাজার)"
  override val shareWithEmailLabel = "যার ইমেইলে পাঠাবেন (ঐচ্ছিক)"
  override val shareWithEmailHint = "যেমন: wife@gmail.com — দিলে তার অ্যাপেও লাইভ দেখাবে"
  override val sharedByLabel = "পাঠিয়েছেন"
  override val sharedWithLabel = "শেয়ার করা হয়েছে"
  override val noMarketListsTitle = "এখনও কোনো বাজার লিস্ট নেই"
  override val noMarketListsBody = "নিজে ব্যবহার করতে বা আপনার স্ত্রী/পরিবারের ইমেইলে পাঠাতে নতুন বাজার লিস্ট তৈরি করুন।"
  override val itemNameLabel = "পণ্যের নাম (যেমন: আলু, ডিম)"
  override val itemQtyLabel = "পরিমাণ (যেমন: ২ কেজি)"
  override val itemPriceOptionalLabel = "দাম (ঐচ্ছিক)"
  override val addItemButton = "যোগ করুন"
  override val boughtLabel = "কেনা হয়েছে"
  override val remainingItemsLabel = "বাকি আছে"
  override val estimatedTotalLabel = "মোট হিসাব"
  override val logAsExpenseButton = "বাজার খরচ হিসেবে সেভ করুন"
  override val markCompletedButton = "বাজার সম্পন্ন"
  override val reopenListButton = "পুনরায় চালু করুন"
  override val shareListTitle = "ইমেইলে লিস্ট পাঠান"
  override val shareListButton = "শেয়ার আপডেট"
  override val shareViaTextButton = "টেক্সট শেয়ার"
  override val marketExpenseSaved = "বাজারের মোট খরচ হিসেবে সেভ হয়েছে!"
  override val confirmDeleteMarketListTitle = "বাজার লিস্ট মুছে ফেলবেন?"
  override val confirmDeleteMarketListMessage = "এই বাজার লিস্ট ও সব আইটেম স্থায়ীভাবে মুছে যাবে।"
  override val editItemTitle = "আইটেম সম্পাদনা"
  override val completedBadge = "সম্পন্ন"
  override val activeBadge = "চলমান"
  override val sharedListSavedMsg = "বাজার লিস্ট শেয়ার আপডেট হয়েছে"
  override val splitByCategoryOption = "ক্যাটাগরি অনুযায়ী আলাদা সেভ করুন"
  override val singleCategoryOption = "একটি ক্যাটাগরিতে মোট সেভ করুন"
  override val marketSplitExpenseSaved = "ক্যাটাগরি অনুযায়ী সবগুলো খরচ সেভ হয়েছে!"
  override val itemCategoryLabel = "ক্যাটাগরি"
  override val navDashboard = "ড্যাশবোর্ড"
  override val navTransactions = "লেনদেন"
  override val navExpensesList = "খরচসমূহ"
  override val navIncomesList = "আয়সমূহ"
  override val navAccounts = "অ্যাকাউন্ট"
  override val navCategories = "ক্যাটাগরি"
  override val navLoansDebts = "ধার ও দেনা-পাওনা"
  override val navLedger = "খতিয়ান (Ledger)"
  override val navTrialBalance = "রেওয়ামিল (Trial Balance)"
  override val navTrash = "ট্র্যাশ (৩০ দিন)"
  override val drawerSectionMain = "প্রধান মেনু"
  override val drawerSectionLoans = "ঋণ ও লেনদেন"
  override val drawerSectionReports = "রিপোর্ট ও হিসাব"
  override val drawerSectionSettings = "সেটিংস ও ডাটা"
  override val periodThisMonth = "এই মাস"
  override val periodLastMonth = "গত মাস"
  override val periodThisQuarter = "এই ত্রৈমাসিক"
  override val periodThisYear = "এই বছর"
  override val periodCustomMonth = "নির্দিষ্ট মাস"
  override val todaysActivityTitle = "আজকের হিসাব"
  override val netCashFlowTitle = "নেট ক্যাশ ফ্লো"
  override val surplusLabel = "উদ্বৃত্ত"
  override val deficitLabel = "ঘাটতি"
  override val incomeVsExpenseTitle = "আয় বনাম খরচ"
  override val barChartLabel = "বার"
  override val lineChartLabel = "লাইন"
  override val recentTransactionsTitle = "সাম্প্রতিক লেনদেন"
  override val viewAllButton = "সব দেখুন"
  override val netIncomeLabel = "নেট আয়"
  override val totalIncomeLabel = "মোট আয়"
  override val savingsRateLabel = "সঞ্চয় হার"
  override val topCategoriesTitle = "শীর্ষ ক্যাটাগরি"
  override val catSalary = "বেতন"
  override val catBusiness = "ব্যবসা"
  override val catInvestment = "বিনিয়োগ ও মুনাফা"
  override val catOtherIncome = "অন্যান্য আয়"
  override val categoryTypeLabel = "ক্যাটাগরির ধরন"
  override val expenseCategoryType = "খরচের"
  override val incomeCategoryType = "আয়ের"
  override val customBadge = "কাস্টম"
  override val allFilterTab = "সব"
  override val singleModeTab = "একটি"
  override val multipleModeTab = "একাধিক (ব্যাচ)"
  override val savedBadgeCount = "টি সেভ হয়েছে"
  override val addAnotherHint = "সেভ করে পরবর্তী এন্ট্রি লিখুন"
  override val timeLabel = "সময়"
  override val dueDateOptionalLabel = "পরিশোধের সম্ভাব্য তারিখ (ঐচ্ছিক)"
  override val clearDueDate = "তারিখ মুছুন"
  override val titleOptionalHint = "খালি রাখলে ক্যাটাগরির নাম বসবে"
  override val selectAccountLabel = "অ্যাকাউন্ট নির্বাচন"
  override val accountsLabel = "অ্যাকাউন্ট"
  override val addAccountButton = "নতুন অ্যাকাউন্ট"
  override val editAccountTitle = "অ্যাকাউন্ট সম্পাদনা"
  override val accountNameLabel = "অ্যাকাউন্টের নাম (যেমন: DBBL, বিকাশ)"
  override val accountTypeLabel = "অ্যাকাউন্টের ধরন"
  override val openingBalanceLabel = "শুরুর ব্যালেন্স (Opening Balance)"
  override val presetsLabel = "দ্রুত প্রিসেট"
  override val confirmDeleteAccountTitle = "অ্যাকাউন্ট মুছে ফেলবেন?"
  override val confirmDeleteAccountMessage = "এই কাস্টম অ্যাকাউন্টটি মুছে যাবে (আগের লেনদেন মূল চ্যানেলে থাকবে)।"
  override val searchIconsHint = "আইকন খুঁজুন..."
  override val customHexHint = "হেক্স কোড (যেমন 0E7C5B)"
  override val sortLabel = "সাজান"
  override val sortNewest = "নতুন আগে"
  override val sortOldest = "পুরোনো আগে"
  override val sortAmountHigh = "বেশি টাকা"
  override val sortAmountLow = "কম টাকা"
  override val selectedCountLabel = "টি নির্বাচিত"
  override val bulkDeleteButton = "মুছে ফেলুন"
  override val loadMoreButton = "আরও দেখুন"
  override val overdueBadge = "মেয়াদোত্তীর্ণ"
  override val overdueCountLabel = "মেয়াদোত্তীর্ণ"
  override val partialBadge = "আংশিক"
  override val unpaidBadge = "বাকি"
  override val dueDateLabel = "শেষ তারিখ"
  override val allAccountsOption = "সব অ্যাকাউন্ট"
  override val receiptsLabel = "জমা (Receipts)"
  override val paymentsLabel = "খরচ (Payments)"
  override val netChangeLabel = "নেট পরিবর্তন"
  override val closingBalanceLabel = "সমাপনী ব্যালেন্স"
  override val runningBalanceLabel = "চলমান ব্যালেন্স"
  override val exportLedgerCsv = "খতিয়ান CSV"
  override val exportTrialBalanceCsv = "রেওয়ামিল CSV"
  override val balancedBadge = "সমন্বিত (Balanced)"
  override val unbalancedBadge = "অসমন্বিত"
  override val debitLabel = "ডেবিট (Debit)"
  override val creditLabel = "ক্রেডিট (Credit)"
  override val assetsSection = "সম্পদ ও নগদ (Assets)"
  override val liabilitiesSection = "দেনা ও মূলধন (Liabilities & Equity)"
  override val revenueSection = "আয় (Revenue)"
  override val operatingExpensesSection = "পরিচালন খরচ (Expenses)"
  override val trashEmptyTitle = "ট্র্যাশ খালি আছে"
  override val trashEmptyBody = "মুছে ফেলা লেনদেন এখানে ৩০ দিন সংরক্ষিত থাকে।"
  override val restoreButton = "ফিরিয়ে আনুন"
  override val deleteForeverButton = "স্থায়ীভাবে মুছুন"
  override val emptyTrashButton = "ট্র্যাশ খালি করুন"
  override val daysRemainingSuffix = "দিন বাকি"
  override val restoredMessage = "সফলভাবে ফিরিয়ে আনা হয়েছে"
  override val movedToTrashMessage = "ট্র্যাশে সরানো হয়েছে"
  override val displayPreferencesSection = "অ্যাপ পছন্দসমূহ ও প্রাইভেসি"
  override val showNotesInListLabel = "তালিকায় নোট দেখান"
  override val use24HourTimeLabel = "২৪-ঘণ্টা সময় ফরম্যাট"
  override val itemsPerPageLabel = "প্রতি পেজে আইটেম"
  override val allItemsLabel = "সব"
  override val blurBalancesLabel = "ব্যালেন্স গোপন রাখুন (Blur)"
  override val hapticFeedbackLabel = "ভাইব্রেশন ফিডব্যাক"
  override val dangerZoneSection = "বিপজ্জনক অঞ্চল (Danger Zone)"
  override val clearAllDataButton = "সব ডাটা মুছে ফেলুন"
  override val confirmClearAllTitle = "সব ডাটা স্থায়ীভাবে মুছবেন?"
  override val confirmClearAllMessage = "আপনার সব খরচ, আয়, অ্যাকাউন্ট, দেনা-পাওনা, বাজেট ও ট্র্যাশ চিরতরে মুছে যাবে। আগে ব্যাকআপ নিয়ে নিতে পারেন।"
  override val allDataClearedMessage = "সব ডাটা মুছে ফেলা হয়েছে"
}

object EnglishStrings : AppStrings {
  override val appName = "Hisab"
  override val tagline = "Simple, ad-free daily expense companion"
  override val navExpenses = "Expenses"
  override val navReports = "Reports"
  override val navBudgets = "Budgets"
  override val navSettings = "Settings"
  override val signInTitle = "Welcome to Hisab"
  override val signInSubtitle = "Ad-free daily expense tracking with offline resilience, cloud sync, PIN lock, and Google Drive backup."
  override val signInWithGoogle = "Sign in with Google"
  override val adFreeBadge = "100% Ad-Free"
  override val featureOfflineTitle = "Offline Ready"
  override val featureOfflineDesc = "Log & view expenses even without internet"
  override val featureCloudTitle = "Cloud Sync"
  override val featureCloudDesc = "Automatically synced across your devices"
  override val featureDriveTitle = "Google Drive Backup"
  override val featureDriveDesc = "One-tap encrypted JSON backup & restore"
  override val onlineSynced = "Online • Synced"
  override val offlineModeActive = "Offline Mode On"
  override val offlineCached = "Offline • Cached Locally"
  override val syncingPending = "Syncing changes..."
  override val thisMonthSpend = "Spent This Month"
  override val budgetRemaining = "Remaining Budget"
  override val budgetExceededBy = "Over Budget By"
  override val setBudgetHint = "Set a monthly budget"
  override val dailyAllowance = "Daily Allowance"
  override val today = "Today"
  override val yesterday = "Yesterday"
  override val allCategories = "All"
  override val searchExpenses = "Search expenses..."
  override val addExpense = "Add Expense"
  override val editExpense = "Edit Expense"
  override val expenseTitleLabel = "Description (e.g., Lunch, Groceries)"
  override val amountLabel = "Amount"
  override val categoryLabel = "Select Category"
  override val paymentMethodLabel = "Payment Method"
  override val dateLabel = "Date"
  override val noteOptionalLabel = "Note (optional)"
  override val save = "Save"
  override val cancel = "Cancel"
  override val delete = "Delete"
  override val confirmDeleteTitle = "Delete Expense?"
  override val confirmDeleteMessage = "This expense record will be permanently removed."
  override val emptyExpensesTitle = "No expenses recorded yet"
  override val emptyExpensesBody = "Tap the '+' button below to log your first daily expense."
  override val addCustomCategory = "New Category"
  override val customCategoryName = "Category Name"
  override val chooseColor = "Choose Color"
  override val chooseIcon = "Choose Icon"
  override val cash = "Cash"
  override val card = "Card"
  override val mobileBanking = "Mobile Pay"
  override val bank = "Bank"
  override val monthlyReportTitle = "Monthly Report"
  override val totalSpent = "Total Spent"
  override val dailyAverage = "Daily Average"
  override val transactionsCount = "Transactions"
  override val topCategory = "Top Category"
  override val vsPrevMonth = "vs last month"
  override val categoryChartTitle = "Spending by Category"
  override val dailyTrendTitle = "Daily Spending Trend"
  override val sixMonthChartTitle = "6-Month Comparison"
  override val categoryBreakdown = "Category Breakdown"
  override val tapBarHint = "Tap any slice or bar to inspect details"
  override val noDataForMonth = "No expenses logged for this month"
  override val budgetSettingsTitle = "Budgets & Reminders"
  override val monthlyBudgetLimit = "Monthly Total Budget"
  override val categoryBudgetsTitle = "Category Budgets"
  override val saveBudget = "Save Budget"
  override val budgetSaved = "Budget saved successfully"
  override val spentOf = "spent of"
  override val reminderSettingsTitle = "Notification Reminders"
  override val dailyReminderLabel = "Daily Expense Reminder"
  override val dailyReminderDesc = "Get a gentle daily reminder to log expenses"
  override val reminderTimeLabel = "Reminder Time"
  override val budgetAlertsLabel = "Budget Threshold Alerts"
  override val budgetAlertsDesc = "Notify when spending crosses 80% or 100%"
  override val notificationPermNeeded = "Notification permission required for reminders"
  override val grantPermission = "Allow"
  override val settingsTitle = "Settings & Backup"
  override val accountSection = "Account"
  override val signOut = "Sign Out"
  override val syncSection = "Cloud Sync & Offline Mode"
  override val cloudSyncToggle = "Auto Cloud Sync"
  override val cloudSyncDesc = "Turn off to run strictly in offline mode"
  override val driveBackupSection = "Google Drive Backup"
  override val backUpToDrive = "Back Up to Google Drive"
  override val restoreFromDrive = "Restore from Google Drive"
  override val lastDriveBackup = "Last Drive Backup"
  override val neverBackedUp = "Not backed up yet"
  override val driveBackupSuccess = "Backed up to Google Drive!"
  override val driveRestoreSuccess = "Restored from Google Drive!"
  override val chooseBackupTitle = "Select Backup to Restore"
  override val noDriveBackups = "No Hisab backups found on Google Drive"
  override val localExportSection = "Export & Local Backup"
  override val exportCsv = "Export CSV (Excel)"
  override val exportJson = "Save JSON Backup"
  override val importJson = "Import JSON Backup"
  override val exportSuccess = "Exported successfully"
  override val importSuccess = "Backup imported successfully"
  override val securitySection = "PIN Security"
  override val pinLockToggle = "Enable PIN Lock"
  override val pinLockDesc = "Require a 4-digit PIN to unlock Hisab"
  override val changePin = "Change PIN"
  override val lockOnBackground = "Lock when app goes to background"
  override val enterPinTitle = "Enter PIN to Unlock"
  override val createPinTitle = "Create a 4-Digit PIN"
  override val confirmPinTitle = "Confirm Your PIN"
  override val pinMismatch = "PINs did not match, try again"
  override val wrongPin = "Incorrect PIN, please try again"
  override val pinSaved = "PIN protection enabled"
  override val appearanceSection = "Theme & Dark Mode"
  override val themeModeLabel = "Appearance"
  override val themeSystem = "System"
  override val themeLight = "Light"
  override val themeDark = "Dark"
  override val themePaletteLabel = "Custom Color Theme"
  override val languageCurrencySection = "Language & Currency"
  override val languageLabel = "Language"
  override val currencyLabel = "Currency"
  override val customCategoriesSection = "Custom Categories"
  override val adFreeNotice = "Hisab is 100% ad-free and keeps your financial data private under your own account."
  override val invalidInputError = "Please enter a valid title and positive amount"
  override val catFood = "Food"
  override val catTransport = "Transport"
  override val catGroceries = "Groceries"
  override val catUtilities = "Bills & Utilities"
  override val catShopping = "Shopping"
  override val catHealth = "Health"
  override val catEducation = "Education"
  override val catEntertainment = "Entertainment"
  override val catFamily = "Family & Gifts"
  override val catOther = "Other"
  override val budgetAlert80Title = "Budget Warning (80%+)"
  override val budgetAlert100Title = "Budget Limit Reached!"
  override val reminderNotificationTitle = "Logged today's expenses?"
  override val reminderNotificationBody = "Take a moment to record your daily spending in Hisab."
  override val themeEmerald = "Emerald"
  override val themeIndigo = "Indigo"
  override val themeTerracotta = "Terracotta"
  override val themeOcean = "Ocean"
  override val themePlum = "Plum"
  override val remainingBalance = "Remaining Balance"
  override val addedBalance = "Added Balance"
  override val addBalance = "Add Balance"
  override val editBalance = "Edit Balance"
  override val balanceTitleLabel = "Source (e.g., Salary, Wallet Top-up)"
  override val balanceFilterChip = "Deposits (+)"
  override val expenseTypeTab = "Expense (-)"
  override val balanceTypeTab = "Balance (+)"
  override val receiveMethodLabel = "Deposit Channel"
  override val confirmDeleteBalanceTitle = "Delete Balance Entry?"
  override val confirmDeleteBalanceMessage = "This balance deposit record will be permanently removed."
  override val overallWalletBalance = "Overall Wallet Balance"
  override val entryTypeExpense = "Expense"
  override val entryTypeIncome = "Balance"
  override val navWallets = "Wallets"
  override val transferTypeTab = "Transfer"
  override val borrowTypeTab = "Borrow"
  override val addTransfer = "Transfer Funds"
  override val editTransfer = "Edit Transfer"
  override val fromAccountLabel = "From Account"
  override val toAccountLabel = "To Account"
  override val sameAccountTransferError = "From and To accounts must be different"
  override val addBorrow = "Borrow Money (Payable)"
  override val editBorrow = "Edit Payable"
  override val lenderNameLabel = "Borrowed From (Person Name)"
  override val borrowAutoPayableHint = "Amount will be added to the selected account and a Payable for this person will be created automatically."
  override val transferFilterChip = "Transfers"
  override val payableFilterChip = "Payables"
  override val walletsScreenTitle = "Wallets & Payables"
  override val accountBalancesTitle = "Account Balances"
  override val totalPayablesLabel = "Total Payables"
  override val unpaidPayablesTitle = "Unpaid Payables"
  override val settledPayablesTitle = "Settled Payables"
  override val noPayablesEmpty = "No active payables"
  override val repayButton = "Pay"
  override val repayPayableTitle = "Repay Payable"
  override val paidFromAccountLabel = "Pay From Account"
  override val paidAmountLabel = "Paid"
  override val dueAmountLabel = "Due"
  override val settledBadge = "Settled"
  override val repaymentHistoryTitle = "Repayment History"
  override val transferHistoryTitle = "Account Transfers"
  override val quickTransferAction = "Transfer"
  override val quickBorrowAction = "Borrow (+)"
  override val confirmDeleteTransferTitle = "Delete Transfer?"
  override val confirmDeleteTransferMessage = "This transfer record will be removed and account balances adjusted."
  override val confirmDeletePayableTitle = "Delete Payable?"
  override val confirmDeletePayableMessage = "This payable and its repayment history will be permanently removed."
  override val confirmDeleteRepaymentTitle = "Delete Repayment?"
  override val confirmDeleteRepaymentMessage = "Removing this repayment will restore the due balance."
  override val entryTypeTransfer = "Transfer"
  override val entryTypeBorrow = "Borrowed"
  override val entryTypeRepayment = "Loan Paid"
  override val walletCashLabel = "Cash (Wallet)"
  override val lendTypeTab = "Lend (-)"
  override val addLend = "Lend Money (Receivable)"
  override val editLend = "Edit Receivable"
  override val borrowerNameLabel = "Lent To (Person Name)"
  override val lendAutoReceivableHint = "Amount will be deducted from the selected account and a Receivable for this person will be created automatically."
  override val lendFromAccountLabel = "Lent From Account"
  override val receivableFilterChip = "Receivables"
  override val totalReceivablesLabel = "Total Receivables"
  override val unpaidReceivablesTitle = "Pending Receivables"
  override val settledReceivablesTitle = "Collected Receivables"
  override val noReceivablesEmpty = "No pending receivables"
  override val collectButton = "Collect"
  override val collectReceivableTitle = "Collect Receivable"
  override val collectedToAccountLabel = "Received Into Account"
  override val collectedAmountLabel = "Collected"
  override val collectionHistoryTitle = "Collection History"
  override val quickLendAction = "Lend (-)"
  override val confirmDeleteReceivableTitle = "Delete Receivable?"
  override val confirmDeleteReceivableMessage = "This receivable and its collection history will be permanently removed."
  override val confirmDeleteCollectionTitle = "Delete Collection?"
  override val confirmDeleteCollectionMessage = "Removing this collection will restore the due receivable balance."
  override val entryTypeLend = "Lent Money"
  override val entryTypeCollection = "Loan Collected"
  override val payablesTab = "Payables (Borrowed)"
  override val receivablesTab = "Receivables (Lent)"
  override val marketListTitle = "Market List"
  override val marketCardTitle = "Bazaar & Grocery List"
  override val marketCardSubtitle = "Create a checklist or share live with family by email"
  override val marketNewListButton = "New List"
  override val marketListNameLabel = "List title (e.g., Weekly Bazaar)"
  override val shareWithEmailLabel = "Send to Email (optional)"
  override val shareWithEmailHint = "e.g., partner@gmail.com — syncs live to their phone"
  override val sharedByLabel = "From"
  override val sharedWithLabel = "Shared with"
  override val noMarketListsTitle = "No market lists yet"
  override val noMarketListsBody = "Create a grocery checklist or send one to your spouse's email so they can shop live."
  override val itemNameLabel = "Item name (e.g., Rice, Eggs)"
  override val itemQtyLabel = "Qty (e.g., 2 kg)"
  override val itemPriceOptionalLabel = "Price (optional)"
  override val addItemButton = "Add Item"
  override val boughtLabel = "Bought"
  override val remainingItemsLabel = "Remaining"
  override val estimatedTotalLabel = "Estimated Total"
  override val logAsExpenseButton = "Save as Bazaar Expense"
  override val markCompletedButton = "Finish Shopping"
  override val reopenListButton = "Reopen List"
  override val shareListTitle = "Send List by Email"
  override val shareListButton = "Update Share"
  override val shareViaTextButton = "Share Text"
  override val marketExpenseSaved = "Saved market total as expense!"
  override val confirmDeleteMarketListTitle = "Delete Market List?"
  override val confirmDeleteMarketListMessage = "This market list and all its items will be permanently removed."
  override val editItemTitle = "Edit Item"
  override val completedBadge = "Done"
  override val activeBadge = "Active"
  override val sharedListSavedMsg = "Market list sharing updated"
  override val splitByCategoryOption = "Split into separate category expenses"
  override val singleCategoryOption = "Save total under one category"
  override val marketSplitExpenseSaved = "Saved expenses across categories!"
  override val itemCategoryLabel = "Category"
  override val navDashboard = "Dashboard"
  override val navTransactions = "Transactions"
  override val navExpensesList = "Expenses"
  override val navIncomesList = "Incomes"
  override val navAccounts = "Accounts"
  override val navCategories = "Categories"
  override val navLoansDebts = "Loans & Debts"
  override val navLedger = "Account Ledger"
  override val navTrialBalance = "Trial Balance"
  override val navTrash = "Trash (30 Days)"
  override val drawerSectionMain = "Main"
  override val drawerSectionLoans = "Debts & Loans"
  override val drawerSectionReports = "Reports & Accounting"
  override val drawerSectionSettings = "Settings & Data"
  override val periodThisMonth = "This Month"
  override val periodLastMonth = "Last Month"
  override val periodThisQuarter = "This Quarter"
  override val periodThisYear = "This Year"
  override val periodCustomMonth = "Custom Month"
  override val todaysActivityTitle = "Today's Activity"
  override val netCashFlowTitle = "Net Cash Flow"
  override val surplusLabel = "Surplus"
  override val deficitLabel = "Deficit"
  override val incomeVsExpenseTitle = "Income vs Expense"
  override val barChartLabel = "Bar"
  override val lineChartLabel = "Line"
  override val recentTransactionsTitle = "Recent Transactions"
  override val viewAllButton = "View All"
  override val netIncomeLabel = "Net Income"
  override val totalIncomeLabel = "Total Income"
  override val savingsRateLabel = "Savings Rate"
  override val topCategoriesTitle = "Top Categories"
  override val catSalary = "Salary"
  override val catBusiness = "Business"
  override val catInvestment = "Investment"
  override val catOtherIncome = "Other Income"
  override val categoryTypeLabel = "Category Type"
  override val expenseCategoryType = "Expense"
  override val incomeCategoryType = "Income"
  override val customBadge = "Custom"
  override val allFilterTab = "All"
  override val singleModeTab = "Single"
  override val multipleModeTab = "Multiple"
  override val savedBadgeCount = "saved"
  override val addAnotherHint = "Save & enter next item"
  override val timeLabel = "Time"
  override val dueDateOptionalLabel = "Due Date (optional)"
  override val clearDueDate = "Clear"
  override val titleOptionalHint = "Leave blank to use category name"
  override val selectAccountLabel = "Select Account"
  override val accountsLabel = "Account"
  override val addAccountButton = "Add Account"
  override val editAccountTitle = "Edit Account"
  override val accountNameLabel = "Account Name (e.g., Savings, bKash)"
  override val accountTypeLabel = "Account Type"
  override val openingBalanceLabel = "Opening Balance"
  override val presetsLabel = "Quick Presets"
  override val confirmDeleteAccountTitle = "Delete Account?"
  override val confirmDeleteAccountMessage = "This custom account will be deleted (existing transactions remain under its channel)."
  override val searchIconsHint = "Search icons..."
  override val customHexHint = "Custom Hex (e.g., 0E7C5B)"
  override val sortLabel = "Sort"
  override val sortNewest = "Newest"
  override val sortOldest = "Oldest"
  override val sortAmountHigh = "Highest"
  override val sortAmountLow = "Lowest"
  override val selectedCountLabel = "selected"
  override val bulkDeleteButton = "Delete Selected"
  override val loadMoreButton = "Load More"
  override val overdueBadge = "Overdue"
  override val overdueCountLabel = "Overdue"
  override val partialBadge = "Partial"
  override val unpaidBadge = "Unpaid"
  override val dueDateLabel = "Due"
  override val allAccountsOption = "All Accounts"
  override val receiptsLabel = "Receipts"
  override val paymentsLabel = "Payments"
  override val netChangeLabel = "Net Change"
  override val closingBalanceLabel = "Closing Balance"
  override val runningBalanceLabel = "Balance"
  override val exportLedgerCsv = "Export Ledger CSV"
  override val exportTrialBalanceCsv = "Export Trial Balance CSV"
  override val balancedBadge = "Balanced"
  override val unbalancedBadge = "Unbalanced"
  override val debitLabel = "Debit"
  override val creditLabel = "Credit"
  override val assetsSection = "Assets & Cash"
  override val liabilitiesSection = "Liabilities & Equity"
  override val revenueSection = "Revenue"
  override val operatingExpensesSection = "Operating Expenses"
  override val trashEmptyTitle = "Trash is Empty"
  override val trashEmptyBody = "Deleted records remain here for 30 days before permanent removal."
  override val restoreButton = "Restore"
  override val deleteForeverButton = "Delete Forever"
  override val emptyTrashButton = "Empty Trash"
  override val daysRemainingSuffix = "days left"
  override val restoredMessage = "Restored successfully"
  override val movedToTrashMessage = "Moved to Trash"
  override val displayPreferencesSection = "Preferences & Privacy"
  override val showNotesInListLabel = "Show Notes in List"
  override val use24HourTimeLabel = "24-Hour Time Format"
  override val itemsPerPageLabel = "Items Per Page"
  override val allItemsLabel = "All"
  override val blurBalancesLabel = "Hide / Blur Balances"
  override val hapticFeedbackLabel = "Haptic Feedback"
  override val dangerZoneSection = "Danger Zone"
  override val clearAllDataButton = "Clear All Data"
  override val confirmClearAllTitle = "Clear All Financial Data?"
  override val confirmClearAllMessage = "This permanently erases all expenses, incomes, accounts, loans, budgets, and trash. Consider exporting a backup first."
  override val allDataClearedMessage = "All data cleared"
}

object HindiStrings : AppStrings {
  override val appName = "हिसाब"
  override val tagline = "दैनिक खर्चों का सरल और सुरक्षित साथी"
  override val navExpenses = "खर्च"
  override val navReports = "रिपोर्ट"
  override val navBudgets = "बजट"
  override val navSettings = "सेटिंग्स"
  override val signInTitle = "हिसाब में आपका स्वागत है"
  override val signInSubtitle = "विज्ञापन-मुक्त दैनिक खर्च ट्रैकिंग, ऑफलाइन मोड, क्लाउड सिंक और Google Drive बैकअप के साथ।"
  override val signInWithGoogle = "Google से साइन इन करें"
  override val adFreeBadge = "100% विज्ञापन मुक्त"
  override val featureOfflineTitle = "ऑफलाइन मोड"
  override val featureOfflineDesc = "बिना इंटरनेट के भी खर्च जोड़ें और देखें"
  override val featureCloudTitle = "क्लाउड सिंक"
  override val featureCloudDesc = "सभी डिवाइस पर डेटा सुरक्षित रूप से सिंक"
  override val featureDriveTitle = "Google Drive बैकअप"
  override val featureDriveDesc = "एक टैप में ड्राइव बैकअप और रीस्टोर"
  override val onlineSynced = "ऑनलाइन • सिंक किया गया"
  override val offlineModeActive = "ऑफलाइन मोड चालू"
  override val offlineCached = "ऑफलाइन • लोकल सेव"
  override val syncingPending = "सिंक हो रहा है..."
  override val thisMonthSpend = "इस महीने का कुल खर्च"
  override val budgetRemaining = "शेष बजट"
  override val budgetExceededBy = "बजट से अधिक"
  override val setBudgetHint = "मासिक बजट सेट करें"
  override val dailyAllowance = "दैनिक सीमा"
  override val today = "आज"
  override val yesterday = "कल"
  override val allCategories = "सभी"
  override val searchExpenses = "खर्च खोजें..."
  override val addExpense = "नया खर्च जोड़ें"
  override val editExpense = "खर्च संपादित करें"
  override val expenseTitleLabel = "विवरण (जैसे: दोपहर का खाना)"
  override val amountLabel = "राशि"
  override val categoryLabel = "श्रेणी चुनें"
  override val paymentMethodLabel = "भुगतान का माध्यम"
  override val dateLabel = "तारीख"
  override val noteOptionalLabel = "नोट (वैकल्पिक)"
  override val save = "सहेजें"
  override val cancel = "रद्द करें"
  override val delete = "हटाएं"
  override val confirmDeleteTitle = "खर्च हटाएं?"
  override val confirmDeleteMessage = "यह खर्च रिकॉर्ड स्थायी रूप से हटा दिया जाएगा।"
  override val emptyExpensesTitle = "अभी तक कोई खर्च नहीं जोड़ा गया"
  override val emptyExpensesBody = "अपना पहला दैनिक खर्च जोड़ने के लिए नीचे '+' बटन दबाएं।"
  override val addCustomCategory = "नई श्रेणी"
  override val customCategoryName = "श्रेणी का नाम"
  override val chooseColor = "रंग चुनें"
  override val chooseIcon = "आइकन चुनें"
  override val cash = "नकद"
  override val card = "कार्ड"
  override val mobileBanking = "मोबाइल पे"
  override val bank = "बैंक"
  override val monthlyReportTitle = "मासिक रिपोर्ट"
  override val totalSpent = "कुल खर्च"
  override val dailyAverage = "दैनिक औसत"
  override val transactionsCount = "लेन-देन"
  override val topCategory = "शीर्ष श्रेणी"
  override val vsPrevMonth = "पिछले महीने से"
  override val categoryChartTitle = "श्रेणी के अनुसार खर्च"
  override val dailyTrendTitle = "दैनिक खर्च का ग्राफ"
  override val sixMonthChartTitle = "पिछले 6 महीनों की तुलना"
  override val categoryBreakdown = "श्रेणी विवरण"
  override val tapBarHint = "विवरण देखने के लिए किसी बार या हिस्से पर टैप करें"
  override val noDataForMonth = "इस महीने के लिए कोई खर्च डेटा नहीं है"
  override val budgetSettingsTitle = "बजट और रिमाइंडर"
  override val monthlyBudgetLimit = "मासिक कुल बजट"
  override val categoryBudgetsTitle = "श्रेणी बजट"
  override val saveBudget = "बजट सहेजें"
  override val budgetSaved = "बजट सफलतापूर्वक सहेजा गया"
  override val spentOf = "खर्च /"
  override val reminderSettingsTitle = "नोटिफिकेशन रिमाइंडर"
  override val dailyReminderLabel = "दैनिक खर्च रिमाइंडर"
  override val dailyReminderDesc = "हर दिन तय समय पर याद दिलाएगा"
  override val reminderTimeLabel = "रिमाइंडर समय"
  override val budgetAlertsLabel = "बजट अलर्ट"
  override val budgetAlertsDesc = "80% या 100% बजट पार होने पर सूचना दें"
  override val notificationPermNeeded = "रिमाइंडर के लिए नोटिफिकेशन अनुमति दें"
  override val grantPermission = "अनुमति दें"
  override val settingsTitle = "सेटिंग्स और बैकअप"
  override val accountSection = "खाता"
  override val signOut = "साइन आउट"
  override val syncSection = "क्लाउड सिंक और ऑफलाइन मोड"
  override val cloudSyncToggle = "ऑटो क्लाउड सिंक"
  override val cloudSyncDesc = "बंद करने पर ऐप पूरी तरह ऑफलाइन चलेगा"
  override val driveBackupSection = "Google Drive बैकअप"
  override val backUpToDrive = "Google Drive पर बैकअप लें"
  override val restoreFromDrive = "Google Drive से रीस्टोर करें"
  override val lastDriveBackup = "अंतिम ड्राइव बैकअप"
  override val neverBackedUp = "अभी तक बैकअप नहीं लिया गया"
  override val driveBackupSuccess = "Google Drive बैकअप सफल रहा!"
  override val driveRestoreSuccess = "Google Drive से डेटा रीस्टोर हो गया!"
  override val chooseBackupTitle = "रीस्टोर करने के लिए बैकअप चुनें"
  override val noDriveBackups = "Google Drive पर कोई बैकअप नहीं मिला"
  override val localExportSection = "डेटा एक्सपोर्ट और लोकल बैकअप"
  override val exportCsv = "CSV एक्सपोर्ट (Excel)"
  override val exportJson = "JSON बैकअप सहेजें"
  override val importJson = "JSON बैकअप इम्पोर्ट करें"
  override val exportSuccess = "सफलतापूर्वक एक्सपोर्ट हो गया"
  override val importSuccess = "बजट और खर्च डेटा इम्पोर्ट हो गया"
  override val securitySection = "पिन सुरक्षा"
  override val pinLockToggle = "पिन लॉक चालू करें"
  override val pinLockDesc = "ऐप खोलने के लिए 4-अंकों का पिन आवश्यक होगा"
  override val changePin = "पिन बदलें"
  override val lockOnBackground = "बैकग्राउंड में जाने पर लॉक करें"
  override val enterPinTitle = "अनलॉक करने के लिए पिन दर्ज करें"
  override val createPinTitle = "नया 4-अंकों का पिन बनाएं"
  override val confirmPinTitle = "पिन की पुष्टि करें"
  override val pinMismatch = "पिन मेल नहीं खाया, पुनः प्रयास करें"
  override val wrongPin = "गलत पिन! पुनः प्रयास करें"
  override val pinSaved = "पिन सुरक्षा चालू हो गई"
  override val appearanceSection = "थीम और डार्क मोड"
  override val themeModeLabel = "डिस्प्ले मोड"
  override val themeSystem = "सिस्टम"
  override val themeLight = "लाइट"
  override val themeDark = "डार्क"
  override val themePaletteLabel = "कस्टम रंग थीम"
  override val languageCurrencySection = "भाषा और मुद्रा"
  override val languageLabel = "भाषा"
  override val currencyLabel = "मुद्रा"
  override val customCategoriesSection = "कस्टम श्रेणियां"
  override val adFreeNotice = "हिसाब 100% विज्ञापन-मुक्त है और आपका डेटा आपके निजी खाते में सुरक्षित रहता है।"
  override val invalidInputError = "कृपया सही विवरण और सकारात्मक राशि दर्ज करें"
  override val catFood = "भोजन"
  override val catTransport = "यात्रा"
  override val catGroceries = "किराना"
  override val catUtilities = "बिल और यूटिलिटी"
  override val catShopping = "खरीदारी"
  override val catHealth = "स्वास्थ्य"
  override val catEducation = "शिक्षा"
  override val catEntertainment = "मनोरंजन"
  override val catFamily = "परिवार और उपहार"
  override val catOther = "अन्य"
  override val budgetAlert80Title = "बजट चेतावनी (80%+)"
  override val budgetAlert100Title = "बजट सीमा पार!"
  override val reminderNotificationTitle = "आज के खर्च दर्ज किए?"
  override val reminderNotificationBody = "हिसाब ऐप में आज के खर्च जोड़ना न भूलें।"
  override val themeEmerald = "एमराल्ड"
  override val themeIndigo = "इंडिगो"
  override val themeTerracotta = "टेराकोटा"
  override val themeOcean = "ओशन"
  override val themePlum = "प्लम"
  override val remainingBalance = "शेष बैलेंस"
  override val addedBalance = "जोड़ा गया बैलेंस"
  override val addBalance = "बैलेंस जोड़ें"
  override val editBalance = "बैलेंस संपादित करें"
  override val balanceTitleLabel = "स्रोत (जैसे: वेतन, नकद जमा)"
  override val balanceFilterChip = "बैलेंस (+)"
  override val expenseTypeTab = "खर्च (-)"
  override val balanceTypeTab = "बैलेंस (+)"
  override val receiveMethodLabel = "जमा माध्यम"
  override val confirmDeleteBalanceTitle = "बैलेंस एंट्री हटाएं?"
  override val confirmDeleteBalanceMessage = "यह बैलेंस रिकॉर्ड स्थायी रूप से हट जाएगा।"
  override val overallWalletBalance = "कुल वॉलेट बैलेंस"
  override val entryTypeExpense = "खर्च"
  override val entryTypeIncome = "बैलेंस"
  override val navWallets = "वॉलेट"
  override val transferTypeTab = "ट्रांसफर"
  override val borrowTypeTab = "उधार (+)"
  override val addTransfer = "खाता ट्रांसफर"
  override val editTransfer = "ट्रांसफर संपादित करें"
  override val fromAccountLabel = "किस खाते से (From)"
  override val toAccountLabel = "किस खाते में (To)"
  override val sameAccountTransferError = "भेजने और प्राप्त करने वाले खाते अलग होने चाहिए"
  override val addBorrow = "उधार लें (Payable)"
  override val editBorrow = "देय संपादित करें"
  override val lenderNameLabel = "किससे उधार लिया (नाम)"
  override val borrowAutoPayableHint = "यह राशि चुने गए खाते में जुड़ जाएगी और स्वतः एक देय (Payable) बन जाएगा।"
  override val transferFilterChip = "ट्रांसफर (⇄)"
  override val payableFilterChip = "उधार / देय"
  override val walletsScreenTitle = "वॉलेट और देय"
  override val accountBalancesTitle = "खातों का बैलेंस"
  override val totalPayablesLabel = "कुल देय (उधार)"
  override val unpaidPayablesTitle = "बकाया देय (Payables)"
  override val settledPayablesTitle = "चुकता देय"
  override val noPayablesEmpty = "कोई बकाया देय नहीं है"
  override val repayButton = "चुकता करें"
  override val repayPayableTitle = "उधार चुकाएं"
  override val paidFromAccountLabel = "किस खाते से भुगतान"
  override val paidAmountLabel = "चुकता"
  override val dueAmountLabel = "शेष देय"
  override val settledBadge = "चुकता"
  override val repaymentHistoryTitle = "भुगतान इतिहास"
  override val transferHistoryTitle = "खाता ट्रांसफर"
  override val quickTransferAction = "ट्रांसफर"
  override val quickBorrowAction = "उधार (+)"
  override val confirmDeleteTransferTitle = "ट्रांसफर हटाएं?"
  override val confirmDeleteTransferMessage = "यह ट्रांसफर रिकॉर्ड स्थायी रूप से हट जाएगा और खाते का बैलेंस अपडेट होगा।"
  override val confirmDeletePayableTitle = "देय रिकॉर्ड हटाएं?"
  override val confirmDeletePayableMessage = "यह देय और इसके भुगतान का विवरण स्थायी रूप से हट जाएगा।"
  override val confirmDeleteRepaymentTitle = "भुगतान हटाएं?"
  override val confirmDeleteRepaymentMessage = "इसे हटाने पर बकाया राशि फिर बढ़ जाएगी।"
  override val entryTypeTransfer = "ट्रांसफर"
  override val entryTypeBorrow = "उधार (Payable)"
  override val entryTypeRepayment = "उधार चुकाया"
  override val walletCashLabel = "नकद (वॉलेट)"
  override val lendTypeTab = "उधार दिया"
  override val addLend = "उधार दें (प्राप्य)"
  override val editLend = "प्राप्य संपादित करें"
  override val borrowerNameLabel = "किसे उधार दिया (नाम)"
  override val lendAutoReceivableHint = "यह राशि चुने गए खाते से कम होगी और स्वतः आपका प्राप्य (Receivable) बन जाएगा।"
  override val lendFromAccountLabel = "किस खाते से दिया"
  override val receivableFilterChip = "प्राप्य (उधार दिया)"
  override val totalReceivablesLabel = "कुल प्राप्य (Receivable)"
  override val unpaidReceivablesTitle = "बकाया प्राप्य"
  override val settledReceivablesTitle = "वसूल प्राप्य"
  override val noReceivablesEmpty = "कोई बकाया प्राप्य नहीं है"
  override val collectButton = "वसूल करें"
  override val collectReceivableTitle = "प्राप्य वसूल करें"
  override val collectedToAccountLabel = "किस खाते में मिला"
  override val collectedAmountLabel = "वसूल"
  override val collectionHistoryTitle = "वसूली इतिहास"
  override val quickLendAction = "उधार दिया (-)"
  override val confirmDeleteReceivableTitle = "प्राप्य रिकॉर्ड हटाएं?"
  override val confirmDeleteReceivableMessage = "यह प्राप्य और इसकी वसूली का विवरण स्थायी रूप से हट जाएगा।"
  override val confirmDeleteCollectionTitle = "वसूली हटाएं?"
  override val confirmDeleteCollectionMessage = "इसे हटाने पर बकाया राशि फिर बढ़ जाएगी।"
  override val entryTypeLend = "उधार दिया"
  override val entryTypeCollection = "वसूली मिली"
  override val payablesTab = "देय (लिया)"
  override val receivablesTab = "प्राप्य (दिया)"
  override val marketListTitle = "बाज़ार लिस्ट"
  override val marketCardTitle = "बाज़ार व किराना लिस्ट"
  override val marketCardSubtitle = "लिस्ट बनाएं या परिवार के ईमेल पर भेजें"
  override val marketNewListButton = "नई लिस्ट"
  override val marketListNameLabel = "लिस्ट का नाम (जैसे: आज का बाज़ार)"
  override val shareWithEmailLabel = "किसके ईमेल पर भेजें (वैकल्पिक)"
  override val shareWithEmailHint = "जैसे: partner@gmail.com — उनके फोन में लाइव दिखेगी"
  override val sharedByLabel = "भेजने वाले"
  override val sharedWithLabel = "शेयर किया गया"
  override val noMarketListsTitle = "अभी कोई बाज़ार लिस्ट नहीं है"
  override val noMarketListsBody = "किराना चेकलिस्ट बनाएं या जीवनसाथी के ईमेल पर भेजें।"
  override val itemNameLabel = "सामान का नाम (जैसे: चावल, आलू)"
  override val itemQtyLabel = "मात्रा (जैसे: 2 किलो)"
  override val itemPriceOptionalLabel = "कीमत (वैकल्पिक)"
  override val addItemButton = "जोड़ें"
  override val boughtLabel = "खरीदा गया"
  override val remainingItemsLabel = "बाकी"
  override val estimatedTotalLabel = "कुल हिसाब"
  override val logAsExpenseButton = "खर्च में सेव करें"
  override val markCompletedButton = "बाज़ार पूरा"
  override val reopenListButton = "पुनः खोलें"
  override val shareListTitle = "ईमेल से लिस्ट भेजें"
  override val shareListButton = "शेयर अपडेट"
  override val shareViaTextButton = "टेक्स्ट शेयर"
  override val marketExpenseSaved = "बाज़ार का कुल खर्च सेव हो गया!"
  override val confirmDeleteMarketListTitle = "बाज़ार लिस्ट हटाएं?"
  override val confirmDeleteMarketListMessage = "यह बाज़ार लिस्ट और सभी आइटम स्थायी रूप से हट जाएंगे।"
  override val editItemTitle = "आइटम संपादित करें"
  override val completedBadge = "पूर्ण"
  override val activeBadge = "सक्रिय"
  override val sharedListSavedMsg = "बाज़ार लिस्ट शेयरिंग अपडेट हुई"
  override val splitByCategoryOption = "श्रेणी के अनुसार अलग-अलग खर्च सहेजें"
  override val singleCategoryOption = "एक ही श्रेणी में कुल खर्च सहेजें"
  override val marketSplitExpenseSaved = "सभी श्रेणियों में खर्च सहेजा गया!"
  override val itemCategoryLabel = "श्रेणी"
  override val navDashboard = "डैशबोर्ड"
  override val navTransactions = "लेन-देन"
  override val navExpensesList = "खर्च"
  override val navIncomesList = "आय"
  override val navAccounts = "खाते"
  override val navCategories = "श्रेणियां"
  override val navLoansDebts = "कर्ज और देनदारी"
  override val navLedger = "खाता बही (Ledger)"
  override val navTrialBalance = "ट्रायल बैलेंस"
  override val navTrash = "ट्रैश (30 दिन)"
  override val drawerSectionMain = "मुख्य"
  override val drawerSectionLoans = "कर्ज और लेन-देन"
  override val drawerSectionReports = "रिपोर्ट और लेखांकन"
  override val drawerSectionSettings = "सेटिंग्स और डेटा"
  override val periodThisMonth = "इस माह"
  override val periodLastMonth = "पिछला माह"
  override val periodThisQuarter = "इस तिमाही"
  override val periodThisYear = "इस वर्ष"
  override val periodCustomMonth = "कस्टम माह"
  override val todaysActivityTitle = "आज की गतिविधि"
  override val netCashFlowTitle = "नेट कैश फ्लो"
  override val surplusLabel = "अधिशेष"
  override val deficitLabel = "घाटा"
  override val incomeVsExpenseTitle = "आय बनाम खर्च"
  override val barChartLabel = "बार"
  override val lineChartLabel = "लाइन"
  override val recentTransactionsTitle = "हाल के लेन-देन"
  override val viewAllButton = "सभी देखें"
  override val netIncomeLabel = "शुद्ध आय"
  override val totalIncomeLabel = "कुल आय"
  override val savingsRateLabel = "बचत दर"
  override val topCategoriesTitle = "शीर्ष श्रेणियां"
  override val catSalary = "वेतन"
  override val catBusiness = "व्यापार"
  override val catInvestment = "निवेश"
  override val catOtherIncome = "अन्य आय"
  override val categoryTypeLabel = "श्रेणी प्रकार"
  override val expenseCategoryType = "खर्च"
  override val incomeCategoryType = "आय"
  override val customBadge = "कस्टम"
  override val allFilterTab = "सभी"
  override val singleModeTab = "एकल"
  override val multipleModeTab = "एकाधिक"
  override val savedBadgeCount = "सहेजे गए"
  override val addAnotherHint = "सहेजें और अगली प्रविष्टि जोड़ें"
  override val timeLabel = "समय"
  override val dueDateOptionalLabel = "नियत तारीख (वैकल्पिक)"
  override val clearDueDate = "हटाएं"
  override val titleOptionalHint = "खाली छोड़ने पर श्रेणी नाम रहेगा"
  override val selectAccountLabel = "खाता चुनें"
  override val accountsLabel = "खाता"
  override val addAccountButton = "नया खाता"
  override val editAccountTitle = "खाता संपादित करें"
  override val accountNameLabel = "खाते का नाम (जैसे: बचत, मोबाइल पे)"
  override val accountTypeLabel = "खाते का प्रकार"
  override val openingBalanceLabel = "प्रारंभिक शेष"
  override val presetsLabel = "त्वरित प्रीसेट"
  override val confirmDeleteAccountTitle = "खाता हटाएं?"
  override val confirmDeleteAccountMessage = "यह कस्टम खाता हट जाएगा (पुराने लेन-देन मूल माध्यम में रहेंगे)।"
  override val searchIconsHint = "आइकन खोजें..."
  override val customHexHint = "कस्टम हेक्स (जैसे 0E7C5B)"
  override val sortLabel = "क्रम"
  override val sortNewest = "नवीनतम"
  override val sortOldest = "सबसे पुराना"
  override val sortAmountHigh = "अधिक राशि"
  override val sortAmountLow = "कम राशि"
  override val selectedCountLabel = "चयनित"
  override val bulkDeleteButton = "चयनित हटाएं"
  override val loadMoreButton = "और दिखाएं"
  override val overdueBadge = "अतिदेय"
  override val overdueCountLabel = "अतिदेय"
  override val partialBadge = "आंशिक"
  override val unpaidBadge = "बकाया"
  override val dueDateLabel = "नियत"
  override val allAccountsOption = "सभी खाते"
  override val receiptsLabel = "प्राप्तियां"
  override val paymentsLabel = "भुगतान"
  override val netChangeLabel = "शुद्ध बदलाव"
  override val closingBalanceLabel = "अंतिम शेष"
  override val runningBalanceLabel = "शेष"
  override val exportLedgerCsv = "लेजर CSV"
  override val exportTrialBalanceCsv = "ट्रायल बैलेंस CSV"
  override val balancedBadge = "संतुलित"
  override val unbalancedBadge = "असंतुलित"
  override val debitLabel = "डेबिट"
  override val creditLabel = "क्रेडिट"
  override val assetsSection = "संपत्ति और नकद"
  override val liabilitiesSection = "देनदारियां और पूंजी"
  override val revenueSection = "राजस्व"
  override val operatingExpensesSection = "संचालन खर्च"
  override val trashEmptyTitle = "ट्रैश खाली है"
  override val trashEmptyBody = "हटाए गए लेन-देन स्थायी रूप से हटने से पहले 30 दिनों तक यहां रहते हैं।"
  override val restoreButton = "पुनर्स्थापित करें"
  override val deleteForeverButton = "हमेशा के लिए हटाएं"
  override val emptyTrashButton = "ट्रैश खाली करें"
  override val daysRemainingSuffix = "दिन शेष"
  override val restoredMessage = "सफलतापूर्वक पुनर्स्थापित"
  override val movedToTrashMessage = "ट्रैश में भेजा गया"
  override val displayPreferencesSection = "प्राथमिकताएं और गोपनीयता"
  override val showNotesInListLabel = "सूची में नोट दिखाएं"
  override val use24HourTimeLabel = "24-घंटे समय प्रारूप"
  override val itemsPerPageLabel = "प्रति पृष्ठ आइटम"
  override val allItemsLabel = "सभी"
  override val blurBalancesLabel = "बैलेंस छिपाएं (Blur)"
  override val hapticFeedbackLabel = "हैप्टिक फीडबैक"
  override val dangerZoneSection = "खतरनाक क्षेत्र"
  override val clearAllDataButton = "सभी डेटा मिटाएं"
  override val confirmClearAllTitle = "सभी डेटा स्थायी रूप से मिटाएं?"
  override val confirmClearAllMessage = "यह सभी खर्च, आय, खाते, कर्ज, बजट और ट्रैश स्थायी रूप से मिटा देगा। पहले बैकअप लेना बेहतर होगा।"
  override val allDataClearedMessage = "सभी डेटा मिटा दिया गया"
}

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
  AppLanguage.BENGALI -> BengaliStrings
  AppLanguage.ENGLISH -> EnglishStrings
  AppLanguage.HINDI -> HindiStrings
}

val LocalStrings = compositionLocalOf<AppStrings> { BengaliStrings }
