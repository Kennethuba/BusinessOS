# Changelog

## 1.0.0 (in progress)

- New layout: Home, Data entry, Pending and More, with a side menu. Landscape adds a "Go to" button.
- Sales and Job orders can be used by any business. Job orders are for a customer or to make stock.
- Money is stored in whole cents. Sales and expenses are voided, never deleted.
- Six-digit PIN login with lockout after wrong attempts, auto-lock timeout, and switching staff by PIN.
- Roles (Owner, Manager, Cashier, Viewer) are enforced in the data layer.
- Setup wizard for a new company, with optional opening cash and M-Pesa balances. A new company starts empty.
- Owner can delete a company after typing its name.
- Forms open full screen instead of as pop-ups.
- Backup flushes the database first. Restore checks the file, keeps a rollback copy, and restarts the app.
- The last crash is saved to a file on the phone.
- Tests: money, roles, phone numbers, PIN and lockout, ledger balance, stock, customer balances, backup checks.

Not in 1.0.0: AI assistant, fingerprint unlock, KRA eTIMS.
