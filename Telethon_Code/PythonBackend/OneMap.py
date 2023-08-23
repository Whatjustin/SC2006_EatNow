'''
Please ensure that onemapsg is installed
'''
from onemapsg import OneMapClient
import configparser
import json
import pandas as pd
import string

import firebase_admin
from firebase_admin import delete_app, get_app
import Firebase


def create_one_map_client():
    config = configparser.ConfigParser()
    config.read(r"config-onemap.ini")
    email = config['OneMap']['email']
    pw = config['OneMap']['password']
    one_map_client = OneMapClient(email, pw)
    return one_map_client

def get_planning_areas(client):
    planning_areas = []
    area_list = client.get_planning_area_names(year=None)
    for a in area_list:
        planning_areas.append(str(a['pln_area_n']))
    return planning_areas

def get_planning_area_coordinates(client):
    coordinates_dict = {}
    area_list = client.get_all_planning_areas(year=None)
    for a in area_list:
        area = a['pln_area_n']
        if a['geojson']:
            geo = json.loads(a['geojson'])
            coordinates = geo['coordinates'][0][0]
            total_longitude = 0
            total_latitude = 0
            for longitude, latitude in coordinates:
                total_longitude += longitude
                total_latitude += latitude
            total_longitude /= len(coordinates)
            total_latitude /= len(coordinates)
            coordinates_dict[area] = [float(total_latitude),float(total_longitude)]
        else:
            coordinates_dict[area] = None
    return coordinates_dict

def get_planning_area_df(planning_areas,coordinates_dict):
    df = pd.DataFrame(columns=['area','latitude','longitude'])
    for a in planning_areas:
        value = coordinates_dict[a]
        area = string.capwords(a)
        if value:
            latitude = value[0]
            longitude = value[1]
            df = df.append({'area':area,'latitude':latitude,'longitude':longitude}, ignore_index=True)
    return df

def get_search_value(client,searchval):
    results = client.search(searchval)['results']
    if results:
        latitude = float(results[0]['LATITUDE'])
        longitude = float(results[0]['LONGTITUDE'])
        return [latitude,longitude]
    else:
        return None

def find_closest_area(latitude,longitude):
    
    ref = Firebase.connect_to_db()
    area_set = ref.child('unique_areas').get()
    
    dist_list = []
    
    for i in range(len(area_set)):
        area_dict = area_set[i]
        area_lat = float(area_dict['latitude'])
        area_long = float(area_dict['longitude'])
        dist = (area_lat - latitude)**2 + (area_long - longitude)**2
        dist_list.append(dist)
    min_value = min(dist_list)
    min_index = dist_list.index(min_value)
    closest_area = area_set[min_index]['area']
    
    delete_app(get_app())
    
    return closest_area

def add_location_to_cloud(location,area):

    ref = Firebase.connect_to_db()
    area_set = ref.child('unique_areas').get()
    
    location = string.capwords(location)
    
    update = False
    
    for i in range(len(area_set)):
        area_dict = area_set[i]
        if (area_dict['area'] == area):
            if not (location in area_dict['locations']):
                locations = area_dict['locations'] + ',' + location
                area_dict['locations'] = locations
                update = True
            break
    
    if update:
        ref.update({
            'unique_areas':area_set
        })
            
    delete_app(get_app())

if __name__ == "__main__":
    client = create_one_map_client()
    areas = get_planning_areas(client)
    coordinates = get_planning_area_coordinates(client)
    df = get_planning_area_df(areas,coordinates)


