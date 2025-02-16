import firebase_admin
from firebase_admin import credentials, firestore
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import re

PATH_TO_CHROMEDRIVER = "chromedriver.exe"
PATH_TO_FIREBASE_CREDENTIALS = "tinker-db-firebase-adminsdk-fbsvc-0afa7f0028.json"

# Aggregated from Notebookcheck.net
BRAND_RATING = {
    "Apple": 1.0,
    "Samsung": 0.9,
    "Lenovo": 0.9,
    "Google": 0.9,
    "Microsoft": 0.8,
    "Nothing": 0.8,
    "LG": 0.7,
    "Asus": 0.7,
    "OnePlus": 0.7,
    "MSI": 0.7,
    "Oppo": 0.6,
    "Vivo": 0.6,
    "Acer": 0.6,
    "Dell": 0.6,
    "HP": 0.6,
    "Realme": 0.6,
}

def main():
    cred = credentials.Certificate(PATH_TO_FIREBASE_CREDENTIALS)
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")

    service = Service(PATH_TO_CHROMEDRIVER)
    driver = webdriver.Chrome(service=service, options=chrome_options)

    load_data("phones", driver, db)
    load_data("laptops", driver, db)
    load_data("tablets", driver, db)

    driver.quit()

def load_data(search_query, driver, db):
    doc_ref = db.collection(search_query)
    url = f"https://www.amazon.ca/s?k={search_query}"

    products = {}

    for i in range(1, 5):
        try:
            driver.get(url + f"&page={i}")
            query_results = driver.find_elements(By.CSS_SELECTOR, "div[data-component-type='s-search-result']")

            for i, product in enumerate(query_results):
                product_name = product.find_element(By.CSS_SELECTOR, "h2.a-text-normal").text
                product_url = product.find_element(By.CSS_SELECTOR, "a.a-link-normal").get_attribute("href")
                products[product_name] = {"url": product_url, "name": product_name}

        except Exception as e:
            print(e)
            continue
        
    for product in products:
        try:
            driver.get(products[product]["url"])

            price_element = driver.find_element(By.CSS_SELECTOR, "span.a-price-whole")
            price = price_element.get_attribute("textContent").strip()
            price = price.replace(',', '').replace('.', '')
            products[product]["price"] = int(price)

            brand_score = 0.5
            for brand, score in BRAND_RATING.items():
                if brand.lower() in product.lower():
                    brand_score = score
                    
            products[product]["brand"] = brand_score
            rating = driver.find_element(By.ID, "averageCustomerReviews").find_element(By.CSS_SELECTOR, "span.a-size-base").get_attribute("textContent")
            products[product]["rating"] = float(rating.strip())
        
            image_element = driver.find_element(By.CLASS_NAME, "a-dynamic-image")
            image_url = image_element.get_attribute("src")
            products[product]["image_url"] = image_url

            details = driver.find_element(By.ID, "prodDetails")
            attributes = details.find_elements(By.CSS_SELECTOR, "tr")

            for attribute in attributes:
                key = attribute.find_element(By.CSS_SELECTOR, "th").text
                value = attribute.find_element(By.CSS_SELECTOR, "td").text
                add_weighted_attribute(products[product], key, value)

        except Exception as e:
            print(e)
            print(f"Error loading product: {products[product]['url']}")
            continue
        
        doc_ref.add(products[product])

def add_weighted_attribute(product, key, value):
    key_lower = key.lower()
    if "ram" in key_lower or "memory" in key_lower:
        ram_value = re.search(r'\d+', value)
        if ram_value:
            product["ram"] = int(ram_value.group())
        else:
            product["ram"] = None
    
    elif "screen size" in key_lower or "screen length" in key_lower or "dimensions" in key_lower or "hard drive" in key_lower:
        screen_size_value = re.search(r'\d+', value)
        if screen_size_value:
            product["size"] = int(screen_size_value.group())
        else:
            product["size"] = None
    elif "storage" in key_lower or "ssd" in key_lower or "hdd" in key_lower:
        storage_value = re.search(r'\d+', value)
        if storage_value:
            storage_size = int(storage_value.group())
            if "tb" in value.lower():
                storage_size *= 1000
            product["storage"] = storage_size
        else:
            product["storage"] = None
    
    elif "product weight" in key_lower or "weight" in key_lower:
        weight_value = re.search(r'(\d+)\s*(lbs|kg|g)', value, re.IGNORECASE)
        if weight_value:
            weight = int(weight_value.group(1))
            unit = weight_value.group(2).lower()
            if unit == "lbs":
                weight *= 453.592
            elif unit == "kg":
                weight *= 1000
            product["weight"] = int(weight)
        else:
            product["weight"] = None
    return
if __name__ == "__main__":
    main()