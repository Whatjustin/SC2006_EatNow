# +
'''
Python Module 
'''
import configparser
import googlemaps # pip install googlemaps
import requests

import OneMap
# -

'''
Creating the Google Places API Client.
Requires 'config-google.ini'.
Returns the Google Client.
'''
def create_client():
    config = configparser.ConfigParser()
    config.read(r"config-google.ini")
    api_key = config['Google']['api_key']
    client = googlemaps.Client(api_key)
    return client


def get_geo_results(keyword):
    client = create_client()
    keyword = '{} in Singapore'.format(keyword)
    response = client.places(query = keyword)
    locations_string = get_location_list(response['results'])
    return locations_string


def get_location_list(results):
    locations_arr = []
    for result in results:
        geo = result['geometry']['location']
        latitude = geo['lat']
        longitude = geo['lng']
        closest_area = OneMap.find_closest_area(latitude,longitude)
        locations_arr.append(closest_area)
    return locations_arr


if __name__ == "__main__":
    test = get_geo_results('Milksha')
    print(test)
