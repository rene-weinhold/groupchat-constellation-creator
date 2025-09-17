# Group Chat Roulette — Annual Planner

This project generates fair, recurring conversation groups (daily / weekly / monthly) for an entire year.  
The goal is **maximum variation**: everyone should meet everyone else about equally often (Social Golfer Problem, solved via
heuristics/constraints).  
Outputs are **human-readable** (Excel `.xlsx`) and **machine-readable** (iCalendar `.ics`). In addition, **statistics** on
distribution are generated.

---

## Features

- **Annual plan** with cadence: daily / weekly (ISO weeks, Mon–Sun) / monthly
- **Fairness logic**: minimizes repeat pairings, balances pair frequencies
- **Export**: Excel (Plan + Statistics), iCalendar (all-day events per round×group)
- **Persistence**: Constellations stored in DB (PostgreSQL)

---

## Usage

Create an annual plan.

**URL** : `/api/v1/constellations`

**Method** : `POST`

**Auth required** : YES (not implemented yet)

## Success Response

**Code** : `200 OK`, later `201 Created`

**Request Body**

To create an annual plan, the following input is needed:

**Fields**

| Field          | Type        | Description                                             |
|----------------|-------------|---------------------------------------------------------|
| people         | List<String | List of participant names (minimum 4)                   |
| numberOfGroups | Integer     | Number of groups to create (minimum 2)                  |
| rotation       | Enum        | Frequency of meetings: `DAILY`, `WEEKLY`, or `MONTHLY`  |
| year           | Integer     | Year for which the plan is to be created (e.g., `2026`) |

**Example Request Body**

```json
{
    "people": [
        "Alice",
        "Bob",
        "Charlie",
        "David",
        "Eve"
    ],
    "numberOfGroups": 2,
    "rotation": "WEEKLY",
    "year": 2026
}
```

**Response**

A .zip file containing:

- A `.xlsx` file with the plan and statistics
- A `.ics` file with calendar events

## Architecture

<img src="/src/main/resources/architecture.png" alt="Architecture Diagram"/>