import pandas as pd
import numpy as np
import os
import time
import dotenv
import ast
from sqlalchemy.sql import text
from datetime import datetime, timedelta
from typing import Dict, List, Union
from sqlalchemy import create_engine, Engine
from smolagents import ToolCallingAgent, OpenAIServerModel, tool
import json
from pydantic import BaseModel, Field

# Create an SQLite database
db_engine = create_engine("sqlite:///munder_difflin.db")

# List containing the different kinds of papers 
paper_supplies = [
    # Paper Types (priced per sheet unless specified)
    {"item_name": "A4 paper",                         "category": "paper",        "unit_price": 0.05},
    {"item_name": "Letter-sized paper",              "category": "paper",        "unit_price": 0.06},
    {"item_name": "Cardstock",                        "category": "paper",        "unit_price": 0.15},
    {"item_name": "Colored paper",                    "category": "paper",        "unit_price": 0.10},
    {"item_name": "Glossy paper",                     "category": "paper",        "unit_price": 0.20},
    {"item_name": "Matte paper",                      "category": "paper",        "unit_price": 0.18},
    {"item_name": "Recycled paper",                   "category": "paper",        "unit_price": 0.08},
    {"item_name": "Eco-friendly paper",               "category": "paper",        "unit_price": 0.12},
    {"item_name": "Poster paper",                     "category": "paper",        "unit_price": 0.25},
    {"item_name": "Banner paper",                     "category": "paper",        "unit_price": 0.30},
    {"item_name": "Kraft paper",                      "category": "paper",        "unit_price": 0.10},
    {"item_name": "Construction paper",               "category": "paper",        "unit_price": 0.07},
    {"item_name": "Wrapping paper",                   "category": "paper",        "unit_price": 0.15},
    {"item_name": "Glitter paper",                    "category": "paper",        "unit_price": 0.22},
    {"item_name": "Decorative paper",                 "category": "paper",        "unit_price": 0.18},
    {"item_name": "Letterhead paper",                 "category": "paper",        "unit_price": 0.12},
    {"item_name": "Legal-size paper",                 "category": "paper",        "unit_price": 0.08},
    {"item_name": "Crepe paper",                      "category": "paper",        "unit_price": 0.05},
    {"item_name": "Photo paper",                      "category": "paper",        "unit_price": 0.25},
    {"item_name": "Uncoated paper",                   "category": "paper",        "unit_price": 0.06},
    {"item_name": "Butcher paper",                    "category": "paper",        "unit_price": 0.10},
    {"item_name": "Heavyweight paper",                "category": "paper",        "unit_price": 0.20},
    {"item_name": "Standard copy paper",              "category": "paper",        "unit_price": 0.04},
    {"item_name": "Bright-colored paper",             "category": "paper",        "unit_price": 0.12},
    {"item_name": "Patterned paper",                  "category": "paper",        "unit_price": 0.15},

    # Product Types (priced per unit)
    {"item_name": "Paper plates",                     "category": "product",      "unit_price": 0.10},  # per plate
    {"item_name": "Paper cups",                       "category": "product",      "unit_price": 0.08},  # per cup
    {"item_name": "Paper napkins",                    "category": "product",      "unit_price": 0.02},  # per napkin
    {"item_name": "Disposable cups",                  "category": "product",      "unit_price": 0.10},  # per cup
    {"item_name": "Table covers",                     "category": "product",      "unit_price": 1.50},  # per cover
    {"item_name": "Envelopes",                        "category": "product",      "unit_price": 0.05},  # per envelope
    {"item_name": "Sticky notes",                     "category": "product",      "unit_price": 0.03},  # per sheet
    {"item_name": "Notepads",                         "category": "product",      "unit_price": 2.00},  # per pad
    {"item_name": "Invitation cards",                 "category": "product",      "unit_price": 0.50},  # per card
    {"item_name": "Flyers",                           "category": "product",      "unit_price": 0.15},  # per flyer
    {"item_name": "Party streamers",                  "category": "product",      "unit_price": 0.05},  # per roll
    {"item_name": "Decorative adhesive tape (washi tape)", "category": "product", "unit_price": 0.20},  # per roll
    {"item_name": "Paper party bags",                 "category": "product",      "unit_price": 0.25},  # per bag
    {"item_name": "Name tags with lanyards",          "category": "product",      "unit_price": 0.75},  # per tag
    {"item_name": "Presentation folders",             "category": "product",      "unit_price": 0.50},  # per folder

    # Large-format items (priced per unit)
    {"item_name": "Large poster paper (24x36 inches)", "category": "large_format", "unit_price": 1.00},
    {"item_name": "Rolls of banner paper (36-inch width)", "category": "large_format", "unit_price": 2.50},

    # Specialty papers
    {"item_name": "100 lb cover stock",               "category": "specialty",    "unit_price": 0.50},
    {"item_name": "80 lb text paper",                 "category": "specialty",    "unit_price": 0.40},
    {"item_name": "250 gsm cardstock",                "category": "specialty",    "unit_price": 0.30},
    {"item_name": "220 gsm poster paper",             "category": "specialty",    "unit_price": 0.35},
]

# Given below are some utility functions you can use to implement your multi-agent system

def generate_sample_inventory(paper_supplies: list, coverage: float = 0.4, seed: int = 137) -> pd.DataFrame:
    """
    Generate inventory for exactly a specified percentage of items from the full paper supply list.

    This function randomly selects exactly `coverage` × N items from the `paper_supplies` list,
    and assigns each selected item:
    - a random stock quantity between 200 and 800,
    - a minimum stock level between 50 and 150.

    The random seed ensures reproducibility of selection and stock levels.

    Args:
        paper_supplies (list): A list of dictionaries, each representing a paper item with
                               keys 'item_name', 'category', and 'unit_price'.
        coverage (float, optional): Fraction of items to include in the inventory (default is 0.4, or 40%).
        seed (int, optional): Random seed for reproducibility (default is 137).

    Returns:
        pd.DataFrame: A DataFrame with the selected items and assigned inventory values, including:
                      - item_name
                      - category
                      - unit_price
                      - current_stock
                      - min_stock_level
    """
    # Ensure reproducible random output
    np.random.seed(seed)

    # Calculate number of items to include based on coverage
    num_items = int(len(paper_supplies) * coverage)

    # Randomly select item indices without replacement
    selected_indices = np.random.choice(
        range(len(paper_supplies)),
        size=num_items,
        replace=False
    )

    # Extract selected items from paper_supplies list
    selected_items = [paper_supplies[i] for i in selected_indices]

    # Construct inventory records
    inventory = []
    for item in selected_items:
        inventory.append({
            "item_name": item["item_name"].lower(),
            "category": item["category"],
            "unit_price": item["unit_price"],
            "current_stock": np.random.randint(200, 800),  # Realistic stock range
            "min_stock_level": np.random.randint(50, 150)  # Reasonable threshold for reordering
        })

    # Return inventory as a pandas DataFrame
    return pd.DataFrame(inventory)

def init_database(db_engine: Engine, seed: int = 137) -> Engine:    
    """
    Set up the Munder Difflin database with all required tables and initial records.

    This function performs the following tasks:
    - Creates the 'transactions' table for logging stock orders and sales
    - Loads customer inquiries from 'quote_requests.csv' into a 'quote_requests' table
    - Loads previous quotes from 'quotes.csv' into a 'quotes' table, extracting useful metadata
    - Generates a random subset of paper inventory using `generate_sample_inventory`
    - Inserts initial financial records including available cash and starting stock levels

    Args:
        db_engine (Engine): A SQLAlchemy engine connected to the SQLite database.
        seed (int, optional): A random seed used to control reproducibility of inventory stock levels.
                              Default is 137.

    Returns:
        Engine: The same SQLAlchemy engine, after initializing all necessary tables and records.

    Raises:
        Exception: If an error occurs during setup, the exception is printed and raised.
    """
    try:
        # ----------------------------
        # 1. Create an empty 'transactions' table schema
        # ----------------------------
        transactions_schema = pd.DataFrame({
            "id": [],
            "item_name": [],
            "transaction_type": [],  # 'stock_orders' or 'sales'
            "units": [],             # Quantity involved
            "price": [],             # Total price for the transaction
            "transaction_date": [],  # ISO-formatted date
        })
        transactions_schema.to_sql("transactions", db_engine, if_exists="replace", index=False)

        # Set a consistent starting date
        initial_date = datetime(2025, 1, 1).isoformat()

        # ----------------------------
        # 2. Load and initialize 'quote_requests' table
        # ----------------------------
        quote_requests_df = pd.read_csv("quote_requests.csv")
        quote_requests_df["id"] = range(1, len(quote_requests_df) + 1)
        quote_requests_df.to_sql("quote_requests", db_engine, if_exists="replace", index=False)

        # ----------------------------
        # 3. Load and transform 'quotes' table
        # ----------------------------
        quotes_df = pd.read_csv("quotes.csv")
        quotes_df["request_id"] = range(1, len(quotes_df) + 1)
        quotes_df["order_date"] = initial_date

        # Unpack metadata fields (job_type, order_size, event_type) if present
        if "request_metadata" in quotes_df.columns:
            quotes_df["request_metadata"] = quotes_df["request_metadata"].apply(
                lambda x: ast.literal_eval(x) if isinstance(x, str) else x
            )
            quotes_df["job_type"] = quotes_df["request_metadata"].apply(lambda x: x.get("job_type", ""))
            quotes_df["order_size"] = quotes_df["request_metadata"].apply(lambda x: x.get("order_size", ""))
            quotes_df["event_type"] = quotes_df["request_metadata"].apply(lambda x: x.get("event_type", ""))

        # Retain only relevant columns
        quotes_df = quotes_df[[
            "request_id",
            "total_amount",
            "quote_explanation",
            "order_date",
            "job_type",
            "order_size",
            "event_type"
        ]]
        quotes_df.to_sql("quotes", db_engine, if_exists="replace", index=False)

        # ----------------------------
        # 4. Generate inventory and seed stock
        # ----------------------------
        inventory_df = generate_sample_inventory(paper_supplies, seed=seed)

        # Seed initial transactions
        initial_transactions = []

        # Add a starting cash balance via a dummy sales transaction
        initial_transactions.append({
            "item_name": None,
            "transaction_type": "sales",
            "units": None,
            "price": 50000.0,
            "transaction_date": initial_date,
        })

        # Add one stock order transaction per inventory item
        for _, item in inventory_df.iterrows():
            initial_transactions.append({
                "item_name": item["item_name"],
                "transaction_type": "stock_orders",
                "units": item["current_stock"],
                "price": item["current_stock"] * item["unit_price"],
                "transaction_date": initial_date,
            })

        # Commit transactions to database
        pd.DataFrame(initial_transactions).to_sql("transactions", db_engine, if_exists="append", index=False)

        # Save the inventory reference table
        inventory_df.to_sql("inventory", db_engine, if_exists="replace", index=False)

        return db_engine

    except Exception as e:
        print(f"Error initializing database: {e}")
        raise

def create_transaction(
    item_name: str,
    transaction_type: str,
    quantity: int,
    price: float,
    date: Union[str, datetime],
) -> int:
    """
    This function records a transaction of type 'stock_orders' or 'sales' with a specified
    item name, quantity, total price, and transaction date into the 'transactions' table of the database.

    Args:
        item_name (str): The name of the item involved in the transaction.
        transaction_type (str): Either 'stock_orders' or 'sales'.
        quantity (int): Number of units involved in the transaction.
        price (float): Total price of the transaction.
        date (str or datetime): Date of the transaction in ISO 8601 format.

    Returns:
        int: The ID of the newly inserted transaction.

    Raises:
        ValueError: If `transaction_type` is not 'stock_orders' or 'sales'.
        Exception: For other database or execution errors.
    """
    try:
        # Convert datetime to ISO string if necessary
        date_str = date.isoformat() if isinstance(date, datetime) else date

        # Validate transaction type
        if transaction_type not in {"stock_orders", "sales"}:
            raise ValueError("Transaction type must be 'stock_orders' or 'sales'")

        # Prepare transaction record as a single-row DataFrame
        transaction = pd.DataFrame([{
            "item_name": item_name,
            "transaction_type": transaction_type,
            "units": quantity,
            "price": price,
            "transaction_date": date_str,
        }])

        # Insert the record into the database
        transaction.to_sql("transactions", db_engine, if_exists="append", index=False)

        # Fetch and return the ID of the inserted row
        result = pd.read_sql("SELECT last_insert_rowid() as id", db_engine)
        return int(result.iloc[0]["id"])

    except Exception as e:
        print(f"Error creating transaction: {e}")
        raise

def get_all_inventory(as_of_date: str) -> Dict[str, int]:
    """
    Retrieve a snapshot of available inventory as of a specific date.

    This function calculates the net quantity of each item by summing 
    all stock orders and subtracting all sales up to and including the given date.

    Only items with positive stock are included in the result.

    Args:
        as_of_date (str): ISO-formatted date string (YYYY-MM-DD) representing the inventory cutoff.

    Returns:
        Dict[str, int]: A dictionary mapping item names to their current stock levels.
    """
    # SQL query to compute stock levels per item as of the given date
    query = """
        SELECT
            item_name,
            SUM(CASE
                WHEN transaction_type = 'stock_orders' THEN units
                WHEN transaction_type = 'sales' THEN -units
                ELSE 0
            END) as stock
        FROM transactions
        WHERE item_name IS NOT NULL
        AND transaction_date <= :as_of_date
        GROUP BY item_name
        HAVING stock > 0
    """

    # Execute the query with the date parameter
    result = pd.read_sql(query, db_engine, params={"as_of_date": as_of_date})

    # Convert the result into a dictionary {item_name: stock}
    return dict(zip(result["item_name"], result["stock"]))

def get_stock_level(item_name: str, as_of_date: Union[str, datetime]) -> pd.DataFrame:
    """
    Retrieve the stock level of a specific item as of a given date.

    This function calculates the net stock by summing all 'stock_orders' and 
    subtracting all 'sales' transactions for the specified item up to the given date.

    Args:
        item_name (str): The name of the item to look up.
        as_of_date (str or datetime): The cutoff date (inclusive) for calculating stock.

    Returns:
        pd.DataFrame: A single-row DataFrame with columns 'item_name' and 'current_stock'.
    """
    # Convert date to ISO string format if it's a datetime object
    if isinstance(as_of_date, datetime):
        as_of_date = as_of_date.isoformat()

    # SQL query to compute net stock level for the item
    stock_query = """
        SELECT
            item_name,
            COALESCE(SUM(CASE
                WHEN transaction_type = 'stock_orders' THEN units
                WHEN transaction_type = 'sales' THEN -units
                ELSE 0
            END), 0) AS current_stock
        FROM transactions
        WHERE item_name = :item_name
        AND transaction_date <= :as_of_date
    """

    # Execute query and return result as a DataFrame
    return pd.read_sql(
        stock_query,
        db_engine,
        params={"item_name": item_name, "as_of_date": as_of_date},
    )

def get_supplier_delivery_date(input_date_str: str, quantity: int) -> str:
    """
    Estimate the supplier delivery date based on the requested order quantity and a starting date.

    Delivery lead time increases with order size:
        - ≤10 units: same day
        - 11–100 units: 1 day
        - 101–1000 units: 4 days
        - >1000 units: 7 days

    Args:
        input_date_str (str): The starting date in ISO format (YYYY-MM-DD).
        quantity (int): The number of units in the order.

    Returns:
        str: Estimated delivery date in ISO format (YYYY-MM-DD).
    """
    # Debug log (comment out in production if needed)
    print(f"FUNC (get_supplier_delivery_date): Calculating for qty {quantity} from date string '{input_date_str}'")

    # Attempt to parse the input date
    try:
        input_date_dt = datetime.fromisoformat(input_date_str.split("T")[0])
    except (ValueError, TypeError):
        # Fallback to current date on format error
        print(f"WARN (get_supplier_delivery_date): Invalid date format '{input_date_str}', using today as base.")
        input_date_dt = datetime.now()

    # Determine delivery delay based on quantity
    if quantity <= 10:
        days = 0
    elif quantity <= 100:
        days = 1
    elif quantity <= 1000:
        days = 4
    else:
        days = 7

    # Add delivery days to the starting date
    delivery_date_dt = input_date_dt + timedelta(days=days)

    # Return formatted delivery date
    return delivery_date_dt.strftime("%Y-%m-%d")

def get_cash_balance(as_of_date: Union[str, datetime]) -> float:
    """
    Calculate the current cash balance as of a specified date.

    The balance is computed by subtracting total stock purchase costs ('stock_orders')
    from total revenue ('sales') recorded in the transactions table up to the given date.

    Args:
        as_of_date (str or datetime): The cutoff date (inclusive) in ISO format or as a datetime object.

    Returns:
        float: Net cash balance as of the given date. Returns 0.0 if no transactions exist or an error occurs.
    """
    try:
        # Convert date to ISO format if it's a datetime object
        if isinstance(as_of_date, datetime):
            as_of_date = as_of_date.isoformat()

        # Query all transactions on or before the specified date
        transactions = pd.read_sql(
            "SELECT * FROM transactions WHERE transaction_date <= :as_of_date",
            db_engine,
            params={"as_of_date": as_of_date},
        )

        # Compute the difference between sales and stock purchases
        if not transactions.empty:
            total_sales = transactions.loc[transactions["transaction_type"] == "sales", "price"].sum()
            total_purchases = transactions.loc[transactions["transaction_type"] == "stock_orders", "price"].sum()
            return float(total_sales - total_purchases)

        return 0.0

    except Exception as e:
        print(f"Error getting cash balance: {e}")
        return 0.0


def generate_financial_report(as_of_date: Union[str, datetime]) -> Dict:
    """
    Generate a complete financial report for the company as of a specific date.

    This includes:
    - Cash balance
    - Inventory valuation
    - Combined asset total
    - Itemized inventory breakdown
    - Top 5 best-selling products

    Args:
        as_of_date (str or datetime): The date (inclusive) for which to generate the report.

    Returns:
        Dict: A dictionary containing the financial report fields:
            - 'as_of_date': The date of the report
            - 'cash_balance': Total cash available
            - 'inventory_value': Total value of inventory
            - 'total_assets': Combined cash and inventory value
            - 'inventory_summary': List of items with stock and valuation details
            - 'top_selling_products': List of top 5 products by revenue
    """
    # Normalize date input
    if isinstance(as_of_date, datetime):
        as_of_date = as_of_date.isoformat()

    # Get current cash balance
    cash = get_cash_balance(as_of_date)

    # Get current inventory snapshot
    inventory_df = pd.read_sql("SELECT * FROM inventory", db_engine)
    inventory_value = 0.0
    inventory_summary = []

    # Compute total inventory value and summary by item
    for _, item in inventory_df.iterrows():
        stock_info = get_stock_level(item["item_name"], as_of_date)
        stock = stock_info["current_stock"].iloc[0]
        item_value = stock * item["unit_price"]
        inventory_value += item_value

        inventory_summary.append({
            "item_name": item["item_name"],
            "stock": stock,
            "unit_price": item["unit_price"],
            "value": item_value,
        })

    # Identify top-selling products by revenue
    top_sales_query = """
        SELECT item_name, SUM(units) as total_units, SUM(price) as total_revenue
        FROM transactions
        WHERE transaction_type = 'sales' AND transaction_date <= :date
        GROUP BY item_name
        ORDER BY total_revenue DESC
        LIMIT 5
    """
    top_sales = pd.read_sql(top_sales_query, db_engine, params={"date": as_of_date})
    top_selling_products = top_sales.to_dict(orient="records")

    return {
        "as_of_date": as_of_date,
        "cash_balance": cash,
        "inventory_value": inventory_value,
        "total_assets": cash + inventory_value,
        "inventory_summary": inventory_summary,
        "top_selling_products": top_selling_products,
    }


def search_quote_history(search_terms: List[str], limit: int = 5) -> List[Dict]:
    """
    Retrieve a list of historical quotes that match any of the provided search terms.

    The function searches both the original customer request (from `quote_requests`) and
    the explanation for the quote (from `quotes`) for each keyword. Results are sorted by
    most recent order date and limited by the `limit` parameter.

    Args:
        search_terms (List[str]): List of terms to match against customer requests and explanations.
        limit (int, optional): Maximum number of quote records to return. Default is 5.

    Returns:
        List[Dict]: A list of matching quotes, each represented as a dictionary with fields:
            - original_request
            - total_amount
            - quote_explanation
            - job_type
            - order_size
            - event_type
            - order_date
    """
    conditions = []
    params = {}

    # Build SQL WHERE clause using LIKE filters for each search term
    for i, term in enumerate(search_terms):
        param_name = f"term_{i}"
        conditions.append(
            f"(LOWER(qr.response) LIKE :{param_name} OR "
            f"LOWER(q.quote_explanation) LIKE :{param_name})"
        )
        params[param_name] = f"%{term.lower()}%"

    # Combine conditions; fallback to always-true if no terms provided
    where_clause = " AND ".join(conditions) if conditions else "1=1"

    # Final SQL query to join quotes with quote_requests
    query = f"""
        SELECT
            qr.response AS original_request,
            q.total_amount,
            q.quote_explanation,
            q.job_type,
            q.order_size,
            q.event_type,
            q.order_date
        FROM quotes q
        JOIN quote_requests qr ON q.request_id = qr.id
        WHERE {where_clause}
        ORDER BY q.order_date DESC
        LIMIT {limit}
    """

    # Execute parameterized query
    with db_engine.connect() as conn:
        result = conn.execute(text(query), params)
        return [dict(row) for row in result]

########################
########################
########################
# YOUR MULTI AGENT STARTS HERE
########################
########################
########################


# Set up and load your env parameters and instantiate your model.
dotenv.load_dotenv(dotenv_path=".env")
openai_api_key = os.getenv("UDACITY_OPENAI_API_KEY")
model = OpenAIServerModel(
    model_id="gpt-4o-mini",
    api_base="https://openai.vocareum.com/v1",
    api_key=openai_api_key,
)

class PaperItem(BaseModel):
    original_name: str = Field(..., description="The original name of the paper item.")
    name: str = Field(..., description="The standardized name of the paper item.")
    quantity: int = Field(..., description="The quantity of the paper item requested.")

class QuoteRequest(BaseModel):
    delivery_date: str = Field(..., description="The requested delivery date in ISO format (YYYY-MM-DD).")
    request_date: str = Field(..., description="The date the quote was requested in ISO format (YYYY-MM-DD).")
    items: List[PaperItem] = Field(..., description="List of paper items requested in the quote.")

class InventoryLevel(BaseModel):
    item_name: str = Field(..., description="The name of the paper item.")
    current_stock: int = Field(..., description="The current stock level of the paper item.")

class UnstockedItem(BaseModel):
    item: str = Field(..., description="The name of the paper item to order.")
    quantity: int = Field(..., description="The quantity of the paper item to order.")
    order_date: str = Field(..., description="The date the order is placed in ISO format (YYYY-MM-DD).")

class ItemOrderedToSupplier(UnstockedItem):
    estimated_delivery_date: str = Field(..., description="The estimated delivery date in ISO format (YYYY-MM-DD).")

class ItemOrderedToSupplierRegistered(ItemOrderedToSupplier):
    successfully_registered: bool = Field(..., description="Indicates if the item was successfully registered.")

class QuoteRecord(BaseModel):
    original_request: str = Field(..., description="The original quote request.")
    total_amount: float = Field(..., description="The total amount for the quote.")
    quote_explanation: str = Field(..., description="Explanation of the quote.")
    job_type: str = Field(..., description="The type of job for the quote.")
    order_size: str = Field(..., description="The size of the order for the quote.")
    event_type: str = Field(..., description="The type of event for the quote.")
    order_date: str = Field(..., description="The date the order was placed in ISO format (YYYY-MM-DD).")

quote_format = {
    "delivery_date": "2025-02-01",
    "request_date": "2025-01-01",
    "items": [
        {
            "original_name": "a4 paper",
            "name": "a4 paper",
            "quantity": 500
        },
        {
            "original_name": "paper cups",
            "name": "paper cups",
            "quantity": 200
        }
    ]
}

"""Set up tools for your agents to use, these should be methods that combine the database functions above
 and apply criteria to them to ensure that the flow of the system is correct."""

@tool
def get_quotes_history_tool(search_terms: List[str], limit: int = 5) -> List[QuoteRecord]:
    """Retrieves historical quotes matching search terms.

    Args:
        search_terms (List[str]): List of keywords to search for in past quotes.
        limit (int, optional): Maximum number of quotes to return. Defaults to 5.

    Returns:
        List[QuoteRecord]: List of matching quotes with details.
    """
    quotes = search_quote_history(search_terms, limit)
    quotes_records = [QuoteRecord(**q) for q in quotes]

    print("===> Retrieved Quotes from Tool:", quotes_records)
    return quotes_records

@tool
def check_inventory_tool(items: List[str], as_of_date: str) -> Dict[str, InventoryLevel]:
    """Checks current inventory levels for items as of a specific date.
    
    Args:
        items (List[str]): List of item names to check inventory for.
        as_of_date (str): Date in ISO format (YYYY-MM-DD) to check inventory levels as of.

    Returns:
        Dict[str, InventoryLevel]: Dictionary of inventory levels for the specified items.
    """
    inventory_levels = {}

    for item in items:
        stock_level = get_stock_level(item, as_of_date)
        inventory_levels[item] = InventoryLevel(item_name=item, current_stock=stock_level["current_stock"].iloc[0])
    
    print("===> Inventory Levels from Tool:", inventory_levels)
    
    return inventory_levels

@tool
def create_supply_order_tool(quantity: int, order_date: str) -> Dict[str, str]:
    """Places a supply order and estimates delivery date.

    Args:
        quantity (int): Number of units to order.
        order_date (str): Date in ISO format (YYYY-MM-DD) when the order is placed.

    Returns:
        Dict[str, str]: Dictionary with order details including estimated delivery date.
    """
    delivery_date = get_supplier_delivery_date(order_date, quantity)
    return {"order_date": order_date, "quantity": quantity, "estimated_delivery_date": delivery_date}   

@tool
def register_supply_order_tool(order_to_register: ItemOrderedToSupplier) -> ItemOrderedToSupplierRegistered:
    """Registers a supply order.

    Args:
        order_to_register: ItemOrderedToSupplier: The supply order details to register.
    Returns:
        ItemOrderedToSupplierRegistered: The registered supply order with success status.    
    """
    success = True
    try: 
        create_transaction(
            item_name=order_to_register.get("item"),
            transaction_type="stock_orders",
            quantity=order_to_register.get("quantity"),
            price=0.0,  # Price can be calculated and updated later
            date=order_to_register.get("order_date"),
        )
    except Exception as e:
        print(f"Error placing supply order for {order_to_register.get('item')}: {e}")
        success = False

    registered_order = ItemOrderedToSupplierRegistered(
        item=order_to_register.get("item"),
        quantity=order_to_register.get("quantity"),
        order_date=order_to_register.get("order_date"),
        estimated_delivery_date=order_to_register.get("estimated_delivery_date"),
        successfully_registered=success
    )

    return registered_order

@tool
def generate_quote_tool(
    original_request: str,
    quote_request: QuoteRequest,
    past_quotes: List[QuoteRecord],
    inventory_levels: Dict[str, InventoryLevel],
    registered_supply_orders: List[ItemOrderedToSupplierRegistered],
) -> QuoteRecord:
    """Generates a new quote based on provided information.

    Args:
        original_request (str): The original customer request.
        quote_request (QuoteRequest): The formatted quote request.
        past_quotes (List[QuoteRecord]): List of relevant past quotes.
        inventory_levels (Dict[str, InventoryLevel]): Current inventory levels.
        registered_supply_orders (List[ItemOrderedToSupplierRegistered]): Registered supply orders.

    Returns:
        QuoteRecord: The generated quote record.
    """
    total_amount = 0
    order_size = 0

    for item in quote_request.items:
        unit_price_query = pd.read_sql(
            "SELECT unit_price FROM inventory WHERE item_name = :item_name",
            db_engine,
            params={"item_name": item.name},
        )
        order_size += item.quantity
        if not unit_price_query.empty:
            unit_price = unit_price_query["unit_price"].iloc[0]
            total_amount += unit_price * item.quantity
        else:
            total_amount += 0.0  # Item not found, price assumed to be 0

    generated_quote = QuoteRecord(
        original_request=original_request,
        total_amount=total_amount,
        quote_explanation='',
        job_type='',
        order_size=order_size,
        event_type='',
        order_date=quote_request.request_date,
    )

    print("===> Generated Quote from Tool:", generated_quote)
    return generated_quote

@tool
def save_quote_tool(quote: QuoteRecord) -> bool:
    """Saves the generated quote to the database.

    Args:
        quote (QuoteRecord): The generated quote record.

    Returns:
        bool: True if the quote was saved successfully, False otherwise.
    """
    success = True

    for item in quote.items:
        try:
            create_transaction(
                item_name=item.name,
                transaction_type="sales",
                quantity=item.quantity,
                price=quote.total_amount,
                date=quote.order_date,
            )
        except Exception as e:
            print(f"Error saving quote: {e}")
            success = success or False

    return success

class QuotesManagementAgent(ToolCallingAgent):
    """Agent for handling quotes."""

    def __init__(self, model: OpenAIServerModel):
        super().__init__(
            tools=[get_quotes_history_tool, register_supply_order_tool, save_quote_tool],
            model=model,
            name="quotes_management_agent",
            description="""
            You are a quotes manager agent. Your role consists of the 
            following main tasks:

            - Reviewing past quotes. Use the tool 
            get_quotes_history_tool to find relevant historical quotes 
            based on customer requests. When using the 
            get_quotes_history_tool, you need to extract relevant search 
            terms from the request.

            - Register supply orders if inventory is insufficient for
            new quotes using the tool register_supply_order_tool.

            - Generating new quotes based on customer requests, 
            available inventory and historical quotes.

            - Saving generated quotes to the database using the tool 
            save_quote_tool.

            In any of the tasks, attempt at most 5 retries if an error
            occurs.
            """
        )
    
    def find_past_quotes(self, search_terms: List[str], limit: int =5) -> List[QuoteRecord]:
        """Find relevant past quotes based on search terms.

        Args:
            search_terms (List[str]): List of keywords to search for in past quotes.
            limit (int, optional): Maximum number of quotes to return. Defaults to 5.

        Returns:
            List[QuoteRecord]: List of matching quotes with details.
        """
        history_prompt = f"""
        Using the following search terms: {search_terms}, find relevant 
        past quotes. Just return a list of past quotes. If no past 
        quotes are found, return an empty list.
        """
        past_quotes = self.run(history_prompt)

        print("===> Past Quotes:", past_quotes)

        return past_quotes
    
    def register_supplies_order(self, orders_to_register: List[ItemOrderedToSupplier]) -> List[ItemOrderedToSupplierRegistered]:
        """Register a supply order.

        Args:
            orders_to_register (List[ItemOrderedToSupplier]): List of items with 'item', 
            'quantity', and 'order_date'.

        Returns:
            List[ItemOrderedToSupplierRegistered]: List of order details including estimated delivery date.
        """

        registered_supply_orders = []

        for order in orders_to_register:
            register_supply_order_prompt = f"""
            Register a supply order for the following item {order}.
            Return the answer as a JSON object with the following format:
            {{
                "item": "{order.item}",
                "order_date": "{order.order_date}",
                "quantity": {order.quantity},
                "estimated_delivery_date": "{order.estimated_delivery_date}",
                "successfully_registered": true/false
            }} 
            """
            registered_order_info = self.run(register_supply_order_prompt)
            registered_order_record = ItemOrderedToSupplierRegistered(**registered_order_info)
            registered_supply_orders.append(registered_order_record)

        return registered_supply_orders

    def generate_quote(
        self,
        original_request: str,
        quote_request: QuoteRequest,
        past_quotes: List[QuoteRecord],
        inventory_levels: Dict[str, InventoryLevel],
        registered_supply_orders: List[ItemOrderedToSupplierRegistered],
    ) -> QuoteRecord:
        """Generate a new quote based on the request, past quotes, inventory, and supply orders.

        Args:
            quote_request (QuoteRequest): The formatted quote request.
            past_quotes (List[QuoteRecord]): List of relevant past quotes.
            inventory_levels (Dict[str, InventoryLevel]): Current inventory levels.
            registered_supply_orders (List[ItemOrderedToSupplierRegistered]): Registered supply orders.
        Returns:
            QuoteRecord: The generated quote record.
        """

        quote_prompt = f"""
        Generate a new quote based on the following information:
        - Original Request: {original_request}
        - Quote Request: {quote_request}
        - Past Quotes: {past_quotes}
        - Inventory Levels: {inventory_levels}
        - Registered Supply Orders: {registered_supply_orders}

        Return the answer as a JSON string with the following format:
        {{
            "original_request": "original request text",
            "total_amount": "0.0",
            "quote_explanation": "explanation of the quote",
            "job_type": "type of job",
            "order_size": "0.0",
            "event_type": "type of event",
            "order_date": "YYYY-MM-DD"
        }}
        Avoid including any additional text outside the JSON object.
        """

        generated_quote = self.run(quote_prompt)

        print("===> Generated Quote:", generated_quote)
        if isinstance(generated_quote, str):
            generated_quote = json.loads(generated_quote)
        return QuoteRecord(**generated_quote)

    def register_quote(self, quote: QuoteRecord) -> bool:
        """Save the generated quote to the database.

        Args:
            quote (QuoteRecord): The generated quote record.

        Returns:
            bool: True if the quote was saved successfully, False otherwise.
        """

        save_prompt = f"""
        Save the following quote to the database: {quote}.
        Use the save_quote_tool to save the quote.
        Return true if the quote was saved successfully, false otherwise.
        """
        save_result = self.run(save_prompt)
        
        return save_result
class InventoryManagementAgent(ToolCallingAgent):
    """Agent for managing inventory."""

    def __init__(self, model: OpenAIServerModel):
        super().__init__(
            tools=[check_inventory_tool],
            model=model,
            name="inventory_management_agent",
            description="""
            You are an inventory management agent. Your role is to 
            monitor and manage the inventory levels of paper supplies. 
            When using the check_inventory_tool, you need to indentify 
            the items and the date in the request.

            Pass the items to the tool as a list of strings and the 
            date as a string in ISO format (YYYY-MM-DD).

            Try at most 5 retries if an error occurs.
            """,   
        )

    def get_inventory_levels(self, items: List[str], as_of_date: str) -> Dict[str, InventoryLevel]:
        """Get inventory levels for a list of items as of a specific date.

        Args:
            items (List[str]): List of item names to check inventory for.
            as_of_date (str): Date in ISO format (YYYY-MM-DD) to check inventory levels as of.
        Returns:
            List[InventoryLevel]: List of InventoryLevel objects representing current stock levels.
        """
        inventory_prompt = f"""
        Check the inventory levels for the following items as of
        {as_of_date}: {items}.
        Return the answer as a list of JSON objects with the following
        format:
        {{
            "item_name": "item name",
            "current_stock": current stock level
        }}
        """

        inventory_levels = self.run(inventory_prompt)
        inventory_levels_records = {}
        for record in inventory_levels:
            inventory_level = InventoryLevel(**record)
            inventory_levels_records[inventory_level.item_name] = inventory_level

        print("===> Inventory levels:", inventory_levels_records)

        return inventory_levels_records

class SupplyManagementAgent(ToolCallingAgent):
    """Agent for managing inventory."""

    def __init__(self, model: OpenAIServerModel):
        super().__init__(
            tools=[create_supply_order_tool],
            model=model,
            name="supply_management_agent",
            description="""
            You are a supply management agent. Your role is to handle 
            supply orders for paper supplies. When using the 
            create_supply_order_tool, you need to indentify the quantity 
            to order and the date of the order in the request. 

            Try at most 5 retries if an error occurs.
            """,   
        )

    def get_delivery_orders(self, items_to_order: List[UnstockedItem]) -> List[ItemOrderedToSupplier]:
        """Get the highest delivery date for a list of items to order.

        Args:
            items_to_order (List[UnstockedItem]): List of items with 'item', 
            'quantity', and 'order_date'.
        Returns:
            List[ItemOrderToSupplier]: List of order details including estimated delivery date.
        """

        supply_orders = []
        for order in items_to_order:
            supply_order_prompt = f"""
            Create a supply order for {order.quantity} units
            of {order.item} on {order.order_date} and 
            provide the estimated delivery date. Return the answer
            as a JSON object with the following format:
            {{
                "item": "{order.item}",
                "order_date": "{order.order_date}",
                "quantity": {order.quantity},
                "estimated_delivery_date": "YYYY-MM-DD"
            }}
            """
            supply_order_info = self.run(supply_order_prompt)
            supply_order_record = ItemOrderedToSupplier(**supply_order_info)
            supply_orders.append(supply_order_record)

        # Return the highest delivery date as a string in ISO format
        return supply_orders

class OrchestratorAgent(ToolCallingAgent):
    """Orchestrator agent for managing Munder Difflin operations."""

    def __init__(self, model: OpenAIServerModel):
        super().__init__(
            tools=[],
            model=model,
            name="orchestrator_agent",
            description="""
            Orchestrator agent for managing Munder Difflin operations. 
            Handles customer requests and delegates to other agents.
            Try at most 5 retries if an error occurs.
            """,
        )
        self.inventory_management = InventoryManagementAgent(model)
        self.quotes_management = QuotesManagementAgent(model)
        self.supply_management = SupplyManagementAgent(model)
        
        ### list of paper supplies
        paper_supplies_list = [item["item_name"].lower() for item in paper_supplies]
        self.paper_supplies_set = set(paper_supplies_list) 

    def format_request(self, user_request: str) -> QuoteRequest:
        """Format the customer request into a structured format.

        Args:
            user_request: The customer's request.

        Returns:
            A formatted request.
        """
        format_prompt = f"""
        Format the following customer request for further processing: {user_request} 
        Map the items in the request to the closest matching items from 
        the following list of paper supplies: {list(self.paper_supplies_set)}. 
        Provide the formatted request using the next example as schema: {quote_format}
        Return the answer as a JSON string object.
        """
        formatted_request = self.run(format_prompt)

        quote_request = json.loads(formatted_request)

        for item in quote_request['items']:
            item['name'] = item['name'].lower()

        quote_request_obj = QuoteRequest(**quote_request)

        print("===> Formatted request:", quote_request_obj)

        return quote_request_obj

    def reply_to_request(self, user_request: str) -> str:
        """
        Handle a customer request by coordinating with other agents.

        Args:
            user_request: The customer's request.

        Returns:
            A response to the customer.
        """

        # Step 1. Format request
        quote_request: QuoteRequest = self.format_request(user_request)

        # Step 2. Get similar past quotes
        search_terms: List[str] = []  # Extract search terms from formatted_request
        for term in quote_request.items:
            search_terms.append(term.name)

        print("===> Search Terms:", search_terms)
        past_quotes = self.quotes_management.find_past_quotes(search_terms)
        
        # Step 3. Check inventory
        inventory_levels: Dict[str, InventoryLevel] = self.inventory_management.get_inventory_levels(search_terms, quote_request.request_date)

        # Step 4. Order supply if needed
        has_to_order = False
        unstocked_items: List[UnstockedItem] = []

        for item in quote_request.items:
            item_name = item.name
            quantity_needed = item.quantity
            current_stock = inventory_levels.get(item_name, InventoryLevel(item_name=item_name, current_stock=0)).current_stock

            if current_stock < quantity_needed:
                order_quantity = quantity_needed - current_stock
                order_date = quote_request.request_date
                has_to_order = True
                unstocked_items.append(
                    UnstockedItem(
                    item=item_name,
                    quantity=order_quantity,
                    order_date=order_date
                ))

        registered_supply_orders = []
        if has_to_order:
            supply_delivery_orders: List[ItemOrderedToSupplier] = self.supply_management.get_delivery_orders(unstocked_items)
            registered_supply_orders = self.quotes_management.register_supplies_order(supply_delivery_orders)
        
        # Step 5. Generate quote
        quote = self.quotes_management.generate_quote(user_request, quote_request, past_quotes, inventory_levels, registered_supply_orders)

        # Step 6. Register quote to database
        self.quotes_management.register_quote(quote)

        response_prompt = f"""
        Generate a response to the customer based on the following quote:
        {quote}
        Provide a concise summary of the quote including total amount and estimated delivery date.
        """
        response = self.run(response_prompt)

        return response
        
# Run your test scenarios by writing them here. Make sure to keep track of them.

def run_test_scenarios():
    
    print("Initializing Database...")
    init_database(db_engine)
    try:
        quote_requests_sample = pd.read_csv("quote_requests_sample.csv")
        quote_requests_sample["request_date"] = pd.to_datetime(
            quote_requests_sample["request_date"], format="%m/%d/%y", errors="coerce"
        )
        quote_requests_sample.dropna(subset=["request_date"], inplace=True)
        quote_requests_sample = quote_requests_sample.sort_values("request_date")
    except Exception as e:
        print(f"FATAL: Error loading test data: {e}")
        return

    quote_requests_sample = pd.read_csv("quote_requests_sample.csv")

    # Sort by date
    quote_requests_sample["request_date"] = pd.to_datetime(
        quote_requests_sample["request_date"]
    )
    quote_requests_sample = quote_requests_sample.sort_values("request_date")

    # Get initial state
    initial_date = quote_requests_sample["request_date"].min().strftime("%Y-%m-%d")
    report = generate_financial_report(initial_date)
    current_cash = report["cash_balance"]
    current_inventory = report["inventory_value"]

    ############
    ############
    ############
    # INITIALIZE YOUR MULTI AGENT SYSTEM HERE
    ############
    ############
    ############

    inquiry_agent = OrchestratorAgent(model)

    results = []
    for idx, row in quote_requests_sample.iterrows():
        request_date = row["request_date"].strftime("%Y-%m-%d")

        print(f"\n=== Request {idx+1} ===")
        print(f"Context: {row['job']} organizing {row['event']}")
        print(f"Request Date: {request_date}")
        print(f"Cash Balance: ${current_cash:.2f}")
        print(f"Inventory Value: ${current_inventory:.2f}")

        # Process request
        request_with_date = f"{row['request']} (Date of request: {request_date})"

        ############
        ############
        ############
        # USE YOUR MULTI AGENT SYSTEM TO HANDLE THE REQUEST
        ############
        ############
        ############

        try:
            response = inquiry_agent.reply_to_request(request_with_date)

            # Update state
            report = generate_financial_report(request_date)
            current_cash = report["cash_balance"]
            current_inventory = report["inventory_value"]

            print(f"Response: {response}")
            print(f"Updated Cash: ${current_cash:.2f}")
            print(f"Updated Inventory: ${current_inventory:.2f}")

            results.append(
                {
                    "request_id": idx + 1,
                    "request_date": request_date,
                    "cash_balance": current_cash,
                    "inventory_value": current_inventory,
                    "response": response,
                }
            )

            time.sleep(1)
        except Exception as e:
            print(f"Error processing request {idx+1}: {e}")
            continue

    # Final report
    final_date = quote_requests_sample["request_date"].max().strftime("%Y-%m-%d")
    final_report = generate_financial_report(final_date)
    print("\n===== FINAL FINANCIAL REPORT =====")
    print(f"Final Cash: ${final_report['cash_balance']:.2f}")
    print(f"Final Inventory: ${final_report['inventory_value']:.2f}")

    # Save results
    pd.DataFrame(results).to_csv("test_results.csv", index=False)
    return results


if __name__ == "__main__":
    results = run_test_scenarios()