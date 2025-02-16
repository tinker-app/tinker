import firebase_admin
from firebase_admin import credentials, firestore

PATH_TO_FIREBASE_CREDENTIALS = "tinker-db-firebase-adminsdk-fbsvc-0afa7f0028.json"

def main():
    cred = credentials.Certificate(PATH_TO_FIREBASE_CREDENTIALS)
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    process_data("phones", db)
    process_data("laptops", db)
    process_data("tablets", db)

def normalize_value(value, min_val, max_val):
    if max_val == min_val: return 0
    return (value - min_val) / (max_val - min_val)

def process_data(search_query, db):
    products = {
        "price": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "rating": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "ram": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "storage": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "size": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "weight": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
        "brand": {"MIN": float('inf'), "MAX": float('-inf'), "COUNT": 0, "SUM": 0},
    }

    collection = db.collection(search_query)
    for document in collection.stream():
        document_dict = document.to_dict()
        for key in products.keys():
            if key in document_dict and document_dict[key] is not None:
                products[key]["MIN"] = min(products[key]["MIN"], document_dict[key])
                products[key]["MAX"] = max(products[key]["MAX"], document_dict[key])
                products[key]["SUM"] += document_dict[key]
                products[key]["COUNT"] += 1
    
    averages = {}
    for key in products.keys():
        if products[key]["COUNT"] > 0: averages[key] = products[key]["SUM"] / products[key]["COUNT"]
        else: averages[key] = 0

    batch = db.batch()
    for document in collection.stream():
        doc_ref = collection.document(document.id)
        document_dict = document.to_dict()
        updates = {}
        
        for key in products.keys():
            if key not in document_dict or document_dict[key] is None: value = averages[key]
            else: value = document_dict[key]
            if products[key]["MIN"] != float('inf') and products[key]["MAX"] != float('-inf'):
                normalized_value = normalize_value(value, products[key]["MIN"], products[key]["MAX"])
                updates[key] = normalized_value
        
        if updates:
            batch.update(doc_ref, updates)
    
    batch.commit()

if __name__ == "__main__":
    main()