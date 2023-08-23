import configparser
import asyncio
from datetime import date, datetime, timezone, timedelta
import pandas as pd
import re 
import json
import base64

from telethon import TelegramClient
from telethon.extensions import BinaryReader
from telethon.sessions import StringSession

import firebase_admin
from firebase_admin import delete_app, get_app
import Firebase

import GoogleMap

import OneMap


DAYS_ADDED = 7


def read_config():
    # Reading Configs
    config = configparser.ConfigParser()
    config.read(r"config-telegram.ini")

    # Setting configuration values
    api_id = config['Telegram']['api_id']
    api_hash = config['Telegram']['api_hash']
    session_string = config['Telegram']['session_string']
    channel = config['Telegram']['channel']
    
    return session_string,api_id,api_hash,channel


async def create_client(session_string,api_id,api_hash):
    client = TelegramClient(StringSession(session_string), api_id, api_hash)
    return client


async def get_food_messages(client,channel):
    await client.start()
    messages = await client.get_messages(channel, limit= 10)
    image,text,date = await get_food_details(client,messages)
    await client.disconnect()
    return image,text,date


'''
Note: TimeZone in UTC.
Example: datetime.strptime('14.05.2019 14:00:00', '%d.%m.%Y %H:%M:%S').replace(tzinfo=timezone.utc)
'''
async def get_food_messages_from_date(client,date,channel):
    await client.start()
    current_date = datetime.utcnow()
    
    pre_first_msg = await client.get_messages(channel, offset_date=date, limit=1)
    first_msg = await client.get_messages(channel, min_id=pre_first_msg[0].id, limit=1, reverse=True)
    last_msg = await client.get_messages(channel, offset_date=current_date, limit=1)
    
    try:
        between = await client.get_messages(channel, min_id=first_msg[0].id, max_id=last_msg[0].id)
        messages = last_msg + between + first_msg
    except:
        messages = last_msg + first_msg
    
    image,text,date = await get_food_details(client,messages)
    await client.disconnect()
    return image,text,date


async def get_food_details(client,messages):
    image = []
    text = []
    date = []
    for message in messages:
        photo = await client.download_media(message.photo, file=bytes)
        image.append(photo)
        text.append(message.text)
        date.append(message.date)
    return image,text,date


def extract_text_details(text):
    brand_arr = []
    cuisine_arr = []
    start_date_arr = []
    end_date_arr = []
    location_arr = []
    for text_string in text:
        text_string = text_string.split('\n')
        brand = ''
        cuisine = ''
        start_date = ''
        end_date = ''
        location = ''
        for i in range (len(text_string)):  
            if i == 0:
                brand = text_string[i].strip()
            else:
                if '🍤' in text_string[i]:
                    cuisine = text_string[i].replace('🍤','').strip()
                    if cuisine == '-':
                          cuisine = ''
                elif '🟢' in text_string[i]:
                    start_date = text_string[i].replace('🟢','').strip()
                elif '🔴' in text_string[i]:
                    end_date = text_string[i].replace('🔴','').strip()
                    if end_date == '-':
                        end_date = ''
                elif '📍' in text_string[i]:
                    location = text_string[i].replace('📍','').strip()
                    if location == '-':
                        location = ''
                else:
                    continue
                    
        start_date,end_date = configure_dates(start_date,end_date)
        
        location = configure_location(brand,location)
        
        brand_arr.append(brand)
        cuisine_arr.append(cuisine)
        start_date_arr.append(start_date)
        end_date_arr.append(end_date)
        location_arr.append(location)
        
    return brand_arr,cuisine_arr,start_date_arr,end_date_arr,location_arr


def configure_dates(start_date,end_date):
    start_date = convert_str_to_date(start_date)
    if end_date == '':
        end_date = start_date + timedelta(days=DAYS_ADDED)
    else: 
        end_date = convert_str_to_date(end_date)
        if start_date.time() == end_date.time():
            end_date += timedelta(days=1)
    start_date = start_date.replace(tzinfo=timezone.utc)
    end_date = end_date.replace(tzinfo=timezone.utc)
    return start_date,end_date


def convert_str_to_date(date_string):
    # Find year
    year = re.findall(r"[0-9]{4}", date_string)
    if len(year) > 0:
        Date = datetime.strptime(date_string,'%d %B %Y')
    else:
        year = str(datetime.today().year)
        date_string += ' ' + year 
        Date = datetime.strptime(date_string,'%d %B %Y')
    return Date


def configure_location(brand,location):
    locations = []
    if re.search(r'all',location,re.IGNORECASE) or location == '':
        locations = GoogleMap.get_geo_results(brand)
            
    split_arr = re.split(r'except',location,re.IGNORECASE)
    
    if len(split_arr) == 1:
        if len(locations) == 0:
            split_arr = re.split(r',|and',location,re.IGNORECASE)
            for text in split_arr:
                location_arr = GoogleMap.get_geo_results('text')
                locations += location_arr
    else:
        to_exclude = re.split(r',|and',split_arr[1],re.IGNORECASE)
        for text in to_exclude:
            results = OneMap.get_search_value(OneMap.create_one_map_client(),text)
            if results:
                latitude,longitude = results
                closest_area = OneMap.find_closest_area(latitude,longitude)
                try:
                    locations.remove(closest_area)
                except:
                    pass
    locations_str = ','.join(locations)
    return locations_str


async def update_messages_to_cloud():
    session_string,api_id,api_hash,channel = read_config()
    client = await create_client(session_string,api_id,api_hash)
    
    latest_date,last_id,text_set = get_latest_message_date_id_and_data()
    last_msg_date = latest_date.replace(tzinfo=timezone.utc)
    image,text,date = await get_food_messages_from_date(client,last_msg_date,channel)
    
    update = False
    
    if (len(text) >= 1):
        brand_arr,cuisine_arr,start_date_arr,end_date_arr,location_arr = extract_text_details(text)
        for i in range (len(text)):
            same_message = False
            for j in range(len(text_set)):
                text_dict = text_set[j]
                if text[i] in text_dict['text']:
                    same_message = True
                    break
            if not same_message:
                update = True
                decoded_image=''
                try:
                    decoded_image = base64.encodebytes(image[i]).decode('utf-8')
                except:
                    pass
                last_id += 1
                text_set.append(
                    {
                        'brand': brand_arr[i],
                        'cuisines': cuisine_arr[i],
                        'end_date': end_date_arr[i].replace(tzinfo=timezone.utc).astimezone(tz=None).timestamp(),
                        'image': decoded_image,
                        'locations': location_arr[i],
                        'message_date': date[i].replace(tzinfo=timezone.utc).astimezone(tz=None).timestamp(),
                        'message_id': last_id,
                        'start_date': start_date_arr[i].replace(tzinfo=timezone.utc).astimezone(tz=None).timestamp(),
                        'text': text[i]
                    }
                )
                
                
    if update:
        ref = Firebase.connect_to_db()
        ref.update({
            'deal_messages':text_set
        }) 
        delete_app(get_app())      


def get_latest_message_date_id_and_data():
    ref = Firebase.connect_to_db()
    text_set = ref.child('deal_messages').get()
    maximum = text_set[0]['message_date']
    last_id = 0
    for i in range(len(text_set)):
        text_dict = text_set[i]
        if (maximum < text_dict['message_date']):
            maximum = text_dict['message_date']
        if (last_id < text_dict['message_id']):
            last_id = text_dict['message_id']
    latest_date = datetime.fromtimestamp(maximum)
    delete_app(get_app())
    return latest_date,last_id,text_set


if __name__ == "__main__":
    pass
    # await update_messages_to_cloud()



