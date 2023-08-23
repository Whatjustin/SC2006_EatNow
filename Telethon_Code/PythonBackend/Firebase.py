import firebase_admin
from firebase_admin import credentials, db

def connect_to_db():
    cred = credentials.Certificate('firebase-sdk.json')
    firebase_admin.initialize_app(cred, {
    'databaseURL':"https://sc2006-3a29a-default-rtdb.asia-southeast1.firebasedatabase.app/"
    })
    ref = db.reference('/')
    return ref

