# 🎓 PulseWire: The Simple Guide

> **ELI5** = "Explain Like I'm 5"  
> This document explains everything in PulseWire in the simplest possible terms.

---

## 🎯 What is PulseWire?

### The One-Sentence Answer

**PulseWire is a super-fast post office for stock market data.**

### The Longer Story

Imagine you want to know the price of Apple stock. That information comes from stock exchanges like NYSE. But here's the problem:

```
PROBLEM: Too Many Languages!
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  NYSE speaks "Format A" ──┐                                     │
│  NASDAQ speaks "Format B" ─┼──▶ Your app only speaks ONE format │
│  London speaks "Format C" ─┘                                    │
│  Tokyo speaks "Format D" ──                                     │
│                                                                 │
│  😵 How do you understand them all?                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

SOLUTION: PulseWire is the translator!
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  NYSE ──┐                      ┌──▶ Trading App                 │
│  NASDAQ ─┼──▶ 🏤 PulseWire ────┼──▶ Mobile App                  │
│  London ─┘   (translates &     └──▶ Analytics                   │
│  Tokyo ──    delivers fast)                                     │
│                                                                 │
│  😊 Everyone gets clean, fast data!                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Project Layout

```
Pulsewire/
├── 📁 pulsewire-core/          ← The toolbox (shared utilities)
├── 📁 pulsewire-control-plane/ ← The manager's office (settings)
├── 📁 pulsewire-data-plane/    ← The mail room (where data flows)
├── 📁 pulsewire-frontend/      ← The customer window (web UI)
└── 📁 docs/                    ← You are here!
```

| Module | Real-World Analogy | What It Does |
|--------|-------------------|--------------|
| **core** | Toolbox | Shared code everyone uses |
| **control-plane** | Manager's office | Configure feeds, users, settings |
| **data-plane** | Mail sorting room | Receives, translates, delivers data |
| **frontend** | Customer window | Web interface to see everything |

---

## 📚 What We've Built (Implementation Log)

This section grows as we implement more features. Each entry explains what we built and why.

---

### ✅ US01-01: Feed Adapter SPI (Plugin System)

**📅 Implemented:** February 2026  
**📁 Location:** `pulsewire-data-plane/src/main/java/com/pulsewire/dataplane/adapter/spi/`

#### What Did We Build?

We built a **plugin system** for connecting to different stock exchanges.

#### Why Do We Need This?

Different stock exchanges send data differently:
- NYSE might use a phone-call-style connection (TCP)
- A crypto exchange might use a chat-app-style connection (WebSocket)
- Bloomberg might require special software (Vendor SDK)

Instead of writing separate code for each, we created a **standard interface** that all connections must follow.

#### The Parts We Created

| File | What It Is | Simple Explanation |
|------|-----------|-------------------|
| `TransportType.java` | A list of connection types | Like choosing: phone call, text message, or fax |
| `RawFeedMessage.java` | A message envelope | Contains: the data + when it arrived + message number |
| `FeedEventHandler.java` | Notification callbacks | How the adapter says "I connected!" or "New data!" or "Error!" |
| `FeedAdapter.java` | The job description | Rules every adapter must follow |
| `SyntheticFeedAdapterV2.java` | A practice adapter | Generates fake data for testing |

#### How It Works (The Flow)

```
Step 1: CREATE
┌─────────────────────────────────────────┐
│ You create an adapter for NYSE          │
│ adapter = new NyseAdapter()             │
└─────────────────────────────────────────┘
                    │
                    ▼
Step 2: CONNECT
┌─────────────────────────────────────────┐
│ You tell it to connect and WHO to tell  │
│ adapter.connect(myHandler)              │
│                                         │
│ "Connect to NYSE and tell myHandler     │
│  whenever something happens"            │
└─────────────────────────────────────────┘
                    │
                    ▼
Step 3: EVENTS START FLOWING
┌─────────────────────────────────────────┐
│ The adapter starts calling your handler │
│                                         │
│ 📗 handler.onConnected("NYSE")          │
│ 📬 handler.onMessage("NYSE", data1)     │
│ 📬 handler.onMessage("NYSE", data2)     │
│ 📬 handler.onMessage("NYSE", data3)     │
│    ... hundreds per second ...          │
└─────────────────────────────────────────┘
                    │
                    ▼
Step 4: DISCONNECT
┌─────────────────────────────────────────┐
│ When you're done:                       │
│ adapter.disconnect()                    │
│                                         │
│ 📕 handler.onDisconnected("NYSE", ...)  │
└─────────────────────────────────────────┘
```

#### Why "SPI" (Service Provider Interface)?

It's a fancy Java term for "plugin system":

```
Without SPI (❌ Bad):                    With SPI (✅ Good):
┌─────────────────────────────┐         ┌─────────────────────────────┐
│ PulseWire code KNOWS about  │         │ PulseWire just knows the    │
│ NYSE, NASDAQ, Bloomberg...  │         │ RULES (interface)           │
│                             │         │                             │
│ To add Tokyo exchange:      │         │ To add Tokyo exchange:      │
│ - Edit PulseWire code 😰    │         │ - Just add a new JAR file!  │
│ - Rebuild everything        │         │ - Zero changes to core 🎉   │
│ - High risk of breaking     │         │ - Java finds it auto-magic  │
└─────────────────────────────┘         └─────────────────────────────┘
```

#### Key Concepts

| Concept | Simple Explanation |
|---------|-------------------|
| **Interface** | A job description. "You MUST have these abilities." |
| **Implementation** | Someone who can do the job. |
| **Callback** | "Call me back when something happens" |
| **Heartbeat** | "Are you still there?" ping to detect dead connections |

---

<!-- 
=======================================================================
  📝 TEMPLATE FOR FUTURE IMPLEMENTATIONS
  Copy this section when adding a new user story
=======================================================================

### ✅ US##-##: [Title]

**📅 Implemented:** [Date]  
**📁 Location:** `path/to/code/`

#### What Did We Build?

[One sentence summary]

#### Why Do We Need This?

[Explain the problem it solves in simple terms]

#### The Parts We Created

| File | What It Is | Simple Explanation |
|------|-----------|-------------------|
| `file1.java` | Description | Simple explanation |

#### How It Works (The Flow)

```
[ASCII diagram showing the flow]
```

#### Key Concepts

| Concept | Simple Explanation |
|---------|-------------------|
| **Term1** | Explanation |

---
-->

---

## 🗺️ What's Coming Next

These are the features we haven't built yet:

| Epic | Feature | Status |
|------|---------|--------|
| EP01 | More feed adapters (TCP, WebSocket, etc.) | ⬜ Not started |
| EP02 | Normalizer (translate all formats to one) | ⬜ Not started |
| EP03 | Book Builder (order book state) | ⬜ Not started |
| EP04 | Message backbone (Kafka) | ⬜ Not started |
| EP05 | WebSocket Gateway (serve to apps) | ⬜ Not started |
| EP06 | Control Plane (management APIs) | ⬜ Not started |
| EP07 | Monitoring & Alerting | ⬜ Not started |
| EP08 | Security & Auth | ⬜ Not started |
| EP09 | Replay & Data Quality | ⬜ Not started |

---

## 🔤 Glossary (Dictionary of Terms)

| Term | Simple Meaning |
|------|---------------|
| **Adapter** | A translator that connects to one data source |
| **Backbone** | The central highway for all messages (Kafka) |
| **Callback** | "Hey, call this function when X happens" |
| **Control Plane** | The management/admin side |
| **Data Plane** | The actual data flow side |
| **Feed** | A stream of market data from an exchange |
| **Gateway** | The door where apps connect to get data |
| **Heartbeat** | A "ping" to check if connection is alive |
| **Normalizer** | Translates different formats into one standard format |
| **Order Book** | List of all buy/sell orders for a stock |
| **SPI** | Service Provider Interface = plugin system |
| **Transport** | HOW data is sent (TCP, WebSocket, etc.) |

---

## ❓ FAQ

**Q: Why not just one big program?**  
A: Because different parts need to scale differently. If we get 10x more data, we can add more adapters without touching the web UI.

**Q: Why Java?**  
A: Java is fast, mature, and widely used in finance. It handles multi-threading well, which is critical for low-latency trading.

**Q: What's the difference between control-plane and data-plane?**  
A: Control-plane = slow, rare changes (config, users). Data-plane = fast, constant flow (market data).

**Q: Why do we need tests?**  
A: Financial systems can't have bugs. A mistake could mean millions lost. Tests prove the code works before it goes live.

---

*This document is updated automatically as we implement new features. Last updated: February 2026*
