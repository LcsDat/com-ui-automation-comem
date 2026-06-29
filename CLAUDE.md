# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run full test suite
mvn test

# Run with a custom TestNG suite file
mvn test -DsuiteXmlFile=src/main/resources/MyRunner.xml

# Run a single test class (edit CoMemTestRunner.xml to include only that class, then run)
mvn test

# Generate Allure report after test run
mvn allure:report

# Open Allure report in browser (live)
mvn allure:serve

# Run Jira/Xray importers (compiled classes must be on classpath)
java -cp target/classes:target/dependency/* scripts.xray.XrayImporter --csv /path/file.csv
java -cp target/classes:target/dependency/* scripts.xray.JiraImporter  --csv /path/file.csv

# List available Jira fields for a project
java ... scripts.xray.XrayImporter --list-fields
```

To run a single test, edit `src/main/resources/CoMemTestRunner.xml` to reference only the target class, then run `mvn test`. The suite XML file controls which tests execute — `-Dtest=` alone does not override it when TestNG XML is configured.

Default locale is `vi`; override with `-Dtest.locale=en`.

## Architecture

### Test Framework (`src/main/java/`)

**`cores/`** is the framework foundation. All test code builds on top of it.

- **`BrowserDriver`** — the central driver wrapper. Auto-detects locator strategy (XPath if starts with `/` or `(`, CSS if starts with `#` or `.`, otherwise tries css/className/id/name). Returns `UIElement` (not raw `WebElement`). Browser types are defined in the `Browser` enum; `DriverFactory` creates the actual driver.

- **`UIElement`** — wraps `WebElement` with automatic stale-element retry (up to 2 retries). All interaction should go through this, not raw `WebElement`.

- **`BasePage<T>`** — all page objects extend this. Provides `driver`, `component()` (returns `ComponentProvider`), and `locale(key)` (resolves from current locale).

- **`BaseComponent<T>`** — all reusable UI components extend this. Uses fluent method chaining via `self()` so interactions can be chained: `.click(loc).setText(loc, val).hover(loc)`.

- **`BaseTest`** — all test classes extend this. Provides `initDriver(Browser)`, `logInfo(description, runnable)` (wraps Allure step + Log4j2), assertion methods (`assertTrue`, `assertEquals`, `verifyTrue`, etc.), and `webDriver` / page object fields.

- **`PageProvider.create(Class<T>, driver)`** — factory to instantiate page objects reflectively. Use this instead of `new PageClass(driver)` directly.

- **`WaitHelper`**, **`ActionsHelper`**, **`JsHelper`** — capability modules wired into `BrowserDriver`. Access via `webDriver.waitUntilVisible(loc)`, `webDriver.js().scrollToBottom()`, etc.

### Page Object Model

Pages live in `pages/`, extend `BasePage<T>`, receive `BrowserDriver` in constructor. Components live in `components/`, extend `BaseComponent<T>`.

A test class instantiates pages via `PageProvider.create(...)` in `@BeforeClass`. Components are accessed via `page.component()` (returns a `ComponentProvider` that creates shared component instances).

### Localization

Locale strings are in `src/main/resources/locales/{vi,en}.properties`. `LocaleManager` is thread-local — set per-thread with `LocaleManager.setLocale("en")` or via system property `-Dtest.locale=en`. Both pages and components resolve text via `locale(key)`.

### Reporting / Logging

- `AllureListener` is registered in `CoMemTestRunner.xml`. Allure annotations (`@Feature`, `@Story`, `@Description`, `@Severity`) go on test classes and methods.
- `logInfo(description, runnable)` in `BaseTest` simultaneously writes to Log4j2 and creates an Allure step.
- `Log4j2Manager` wraps separate loggers for info, assertion pass, and assertion fail.

### Xray / Jira Import Scripts (`src/main/java/scripts/xray/`)

Standalone Java utilities — no Selenium dependency. They read a CSV and call Jira/Xray REST APIs.

- **`XrayImporter`** — imports Test issue type, sets test steps, assigns to Xray folders.
- **`JiraImporter`** — imports any issue type (Task, Bug, Story, Test Execution, etc.).
- **`ColumnConfig`** — maps CSV column headers to Jira field names. Two kinds of mappings:
  - *Core roles* (`Role` enum: `SUMMARY`, `ISSUE_TYPE`, `ASSIGNEE`, `LABELS`, `ACTIONS`, `EXPECTED_RESULT`, `TEST_TYPE`, `FOLDER`, `COPY_TESTS_FROM`) — have special handling in import logic.
  - *Dynamic fields* — any CSV column mapped directly to a Jira field via `.field(csvHeader, jiraFieldName, FieldType)`. `FieldType` controls JSON shape: `NAME_OBJECT` (default), `STRING`, `KEY_OBJECT`, `ID_OBJECT`, `ARRAY`, `RAW`.
- **`ImportConfig`** — loads credentials with priority: CLI args > env vars > `xray-config.properties`. Required env vars: `JIRA_BASE_URL`, `JIRA_EMAIL`, `JIRA_API_TOKEN`, `JIRA_PROJECT_KEY`, `XRAY_CLIENT_ID`, `XRAY_CLIENT_SECRET`.

### Test Data

Excel test data is in `src/main/resources/TestCaseReference.xlsx`, read via `ExcelDataReader` / `DataReaderFactory`. CSV data is read via `CsvDataReader`. `DataReader` is the common interface.