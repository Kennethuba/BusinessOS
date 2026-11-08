# 📖 Business OS — Installation Guide & Complete User Manual

Welcome to **Business OS**, a street-smart, offline-first mobile business management system crafted specifically for Kenyan SMEs — auto garages, mechanics, and Kariakor / Gikomba market stalls.

---

## 📑 Table of Contents
1. [Installation Guide](#1-installation-guide)
   - [Method A: Direct APK Installation (Fastest)](#method-a-direct-apk-installation-fastest)
   - [Method B: Running via Android Studio](#method-b-running-via-android-studio)
2. [Getting Started: Account & Workspace Setup](#2-getting-started-account--workspace-setup)
   - [First Launch & Sign Up](#first-launch--sign-up)
   - [Creating Your Business](#creating-your-business)
   - [Switching Between Workspaces](#switching-between-workspaces)
3. [User Manual: Module 1 — Auto Garage](#3-user-manual-module-1--auto-garage)
   - [The Interactive 3D Vehicle Inspector](#the-interactive-3d-vehicle-inspector)
   - [Job Card Pipeline Stages](#job-card-pipeline-stages)
   - [WhatsApp Estimate Approvals](#whatsapp-estimate-approvals)
   - [Vehicle Plate History](#vehicle-plate-history)
4. [User Manual: Module 2 — Kariakor Stall (POS & Deni)](#4-user-manual-module-2--kariakor-stall-pos--deni)
   - [Adding Inventory (Retail vs Wholesale)](#adding-inventory-retail-vs-wholesale)
   - [POS Checkout Flow (Cash, M-Pesa, Split, Credit)](#pos-checkout-flow-cash-m-pesa-split-credit)
   - [The Deni (Credit) Ledger & WhatsApp Reminders](#the-deni-credit-ledger--whatsapp-reminders)
5. [Owner & Security Tools](#5-owner--security-tools)
   - [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
   - [Append-Only Audit Log (with 3D Holographic Shield)](#append-only-audit-log-with-3d-holographic-shield)
   - [Subscription & Tier Management (with 3D Plan Vault)](#subscription--tier-management-with-3d-plan-vault)
6. [Offline Architecture & Sync](#6-offline-architecture--sync)
7. [Troubleshooting & FAQs](#7-troubleshooting--faqs)

---

## 1. Installation Guide

### Method A: Direct APK Installation (Fastest)
The application has already been pre-compiled into a ready-to-install debug APK located on your laptop:
> **APK Path:** `F:\data new\app\build\outputs\apk\debug\app-debug.apk`

#### Option 1: Install to an Android Phone via USB or File Transfer
1. Connect your Android phone to your laptop via USB cable (or share the file via Google Drive / WhatsApp / Bluetooth).
2. Copy `app-debug.apk` to your phone's **Downloads** folder.
3. On your phone, open your **Files** app and tap `app-debug.apk`.
4. If prompted with *"Install unknown apps"*, enable permission for your browser or file manager.
5. Tap **Install**, then tap **Open**.

#### Option 2: Install via Command Line (ADB)
If you have an Android device plugged in with USB debugging enabled, or an Android emulator running:
1. Open PowerShell or Command Prompt.
2. Run:
   ```powershell
   adb install -r "F:\data new\app\build\outputs\apk\debug\app-debug.apk"
   ```

---

### Method B: Running via Android Studio
1. Launch **Android Studio**.
2. Select **File > Open...** and navigate to:
   ```
   F:\data new
   ```
3. Allow Gradle to finish indexing (it will use the configured 4GB heap daemon).
4. Select your connected phone or emulator from the device dropdown at the top.
5. Click the green **Run (▶)** button or press `Shift + F10`.

---

## 2. Getting Started: Account & Workspace Setup

### First Launch & Sign Up
1. When you first launch the app, you will see the **Login Screen**.
2. If this is your first time, tap **"Sign Up"**.
3. Fill in your details:
   - **Full Name** (e.g., *Maina Mwangi*)
   - **Email** (e.g., *maina@garagemail.co.ke*)
   - **Phone Number** (e.g., *0712345678* or *+254712345678*)
4. Tap **"Create Account"**. Your profile is created instantly in the offline local database.

### Creating Your Business
1. After signing up or logging in, you will land on the **Workspace Switcher**.
2. Tap the **"+ Create Business"** button.
3. In the dialog:
   - **Business Name**: Enter your shop or garage name (e.g., *Westlands Auto Hub* or *Kariakor Footwear No. 12*).
   - **Business Type**: Select either:
     - 🚗 **GARAGE**: Tailored for mechanics, job cards, parts, and 3D diagnostics.
     - 🛍️ **STALL**: Tailored for retail/wholesale products, inventory, POS, and customer credit (*Deni*).
   - **Subscription Plan**: Choose **Starter** (free, up to 2 staff, 1 business), **Growth**, or **Enterprise**.
4. Tap **"Create"**.
5. You are assigned as the **OWNER** with full rights.

### Switching Between Workspaces
- You can own multiple businesses or be invited to work at others as a Manager or Staff member.
- Tap the **Workspace Switcher** icon (two horizontal arrows) in the top bar from any screen to switch businesses instantly.
- **Data Isolation**: All sales, job cards, audit logs, and inventory are strictly scoped to the active workspace.

---

## 3. User Manual: Module 1 — Auto Garage

When operating an **Auto Garage**, the dashboard prioritizes active repair pipelines and vehicle histories.

```
       [ Intake ] ──► [ Diagnosis ] ──► [ Parts Assigned ] ──► [ Awaiting Approval ]
                                                                       │
[ Invoiced ] ◄── [ Completed ] ◄───────── [ In Progress ] ◄────────────┘
```

### The Interactive 3D Vehicle Inspector
- **360° Touch Rotation**: Touch and drag anywhere on the 3D car chassis wireframe to rotate the vehicle in full 3D space.
- **Zone Hotspots**: Tap any of the diagnostic nodes around the car:
  - ⚙️ **Engine**
  - 🛑 **Brakes**
  - 🔄 **Transmission / Gearbox**
  - 🛞 **Suspension / Shocks**
  - ⚡ **Electrical / Battery**
- When inside a Job Card, selecting a 3D zone automatically pre-fills that component in the parts assignment window!

### Job Card Pipeline Stages
1. **Intake**: Tap **"+ New Job Card"**, enter the customer's vehicle plate number (e.g., `KDA 554B`), customer name, phone number, and issue description.
2. **Diagnosis**: Mechanic diagnoses the problem and logs preliminary notes.
3. **Parts Assigned**: Add parts and estimated costs (e.g., *Front Brake Pads - KSh 3,500*, *Labor - KSh 1,500*).
4. **Awaiting Approval**: Estimate total is calculated automatically.
5. **In Progress**: Work is authorized and underway.
6. **Completed**: Work is finished and tested.
7. **Invoiced**: Final invoice generated for the customer.

> Stages are enforced in sequential order. You cannot skip backwards without an authorized reason, which is automatically recorded in the Owner Audit Log.

### WhatsApp Estimate Approvals
- While on the **"Awaiting Approval"** stage of a Job Card, tap the green **"Send WhatsApp Estimate"** button.
- The app automatically generates a pre-formatted, URL-encoded message containing the full breakdown and opens WhatsApp with the customer's phone number:
  > *"Habari, this is Westlands Auto Hub. Estimate for vehicle KDA 554B: Front Brake Pads (3,500), Labor (1,500). Total: KSh 5,000. Reply to approve."*

### Vehicle Plate History
- Tap **"Vehicle History"** from the home screen or menu.
- Type any Kenyan number plate (e.g., `KDA 554B`).
- Instantly view every historical job card, past repairs, parts installed, and total spend for that vehicle across all visits.

---

## 4. User Manual: Module 2 — Kariakor Stall (POS & Deni)

When operating a **Kariakor Stall**, the app transforms into a fast, tactile Point of Sale (POS) register.

### Adding Inventory (Retail vs Wholesale)
1. In the POS screen, tap the **"+"** button (Owner/Manager).
2. Enter:
   - **Item Name** (e.g., *Men's Safari Boots - Size 42*)
   - **Retail Price** (e.g., *KSh 2,500*)
   - **Wholesale Price** (e.g., *KSh 1,800*)
   - **Current Stock Quantity** (e.g., *50*)
3. Tap **"Save"**.

### POS Checkout Flow
1. **Search**: Type in the top search bar to quickly find items.
2. **Line Item Selection**: Tap any product to add it to the cart.
3. **Quantity Stepper**: Use `+` and `-` buttons to set the count.
4. **Wholesale / Retail Toggle**: Flip between **Retail** and **Wholesale** pricing with a single tap.
5. **Checkout**:
   - Tap **"Checkout"**. A tactile 3D summary card appears with spring-physics tilt response.
   - Choose payment method:
     - 💵 **Cash**
     - 📱 **M-Pesa** (enter the M-Pesa transaction reference code)
     - 🔀 **Split** (part cash, part M-Pesa)
     - 📝 **Credit (Deni)** (select or create a customer profile)
6. Tap **"Complete Sale"**. Inventory counts deduct immediately.

### The Deni (Credit) Ledger & WhatsApp Reminders
1. Open the **"Deni Ledger"** screen.
2. View all customers with outstanding credit balances and running ledger amounts.
3. **Record a Repayment**: Tap on a customer, enter the amount paid, and their balance reduces instantly.
4. **WhatsApp Reminder**:
   - Tap the WhatsApp button next to any debtor.
   - The app prepares a friendly Swahili/English reminder deep-link:
     > *"Habari John, friendly reminder from Kariakor Footwear. Your outstanding balance is KSh 1,800. Kindly pay via M-Pesa when convenient. Asante!"*

---

## 5. Owner & Security Tools

### Role-Based Access Control (RBAC)
Every action in the repository layer validates the user's role:

| Action | OWNER | MANAGER | STAFF |
|---|:---:|:---:|:---:|
| Create Sales & Job Cards | ✅ | ✅ | ✅ |
| Override Item Prices | ✅ | ✅ | ❌ |
| Edit Inventory Stock | ✅ | ✅ | ❌ |
| Invite Staff Members | ✅ | ❌ | ❌ |
| Delete Records / Cancel Jobs | ✅ | ❌ | ❌ |
| View Audit Log | ✅ | ❌ | ❌ |
| Manage Billing / Upgrades | ✅ | ❌ | ❌ |

### Append-Only Audit Log (with 3D Holographic Shield)
- Accessible **only by the Business Owner**.
- Displays an interactive **3D Holographic Audit Shield** with rotating electron orbits and laser scan lines.
- **Append-Only**: There is no delete button and no edit button. Every critical action (Price Overrides, Stock Adjustments, Job Cancellations, Role Changes) is logged automatically with the actor's name, timestamp, before-value, and after-value.

### Subscription & Tier Management (with 3D Plan Vault)
- View your active tier and limits.
- Features an interactive **3D Plan Vault Diamond Polyhedron** that you can drag to rotate 360°, with dynamic glow colors per tier (Emerald for Starter, Blue for Growth, Gold for Enterprise).
- Compare tiers (limits on number of staff and multi-branch businesses).

---

## 6. Offline Architecture & Sync

- **Local Database is Master**: Business OS uses Room DB on your phone as the single source of truth. The app functions **100% offline** without needing WiFi or mobile data.
- **Real-Time Sync Badge**:
  - 🟢 **"All synced"**: Everything is up to date.
  - 🟡 **"Syncing…"**: Connection detected; pending local records are syncing in the background.
  - 🔴 **"Offline"**: No internet connection; all writes are stored safely in Room and queued.
- When cellular connection returns, [`SyncManager`](file:///F:/data%20new/app/src/main/java/com/example/businessos/sync/SyncManager.kt) automatically synchronizes pending changes.

---

## 7. Troubleshooting & FAQs

**Q: Can I run both a Garage and a Market Stall on the same phone?**  
**A:** Yes! Create one business as `GARAGE` and another as `STALL`. Use the Workspace Switcher in the top bar to toggle between them.

**Q: Does WhatsApp need an API key or paid subscription?**  
**A:** No. Business OS uses native Android deep links (`wa.me`). It opens your installed WhatsApp or WhatsApp Business app with the message pre-filled.

**Q: What happens if my battery dies or the phone reboots mid-job?**  
**A:** All data is committed directly to SQLite Room storage immediately upon each tap. No unsaved changes are lost.

**Q: Can a staff member see how much profit the business made or delete records?**  
**A:** No. Role-based security checks in the data layer block staff members from viewing audit logs, deleting transactions, or modifying baseline prices.
