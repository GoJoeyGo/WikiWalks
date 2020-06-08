import requests
import json
import posts
import unittest
from flask import jsonify, request, Blueprint
from schemas import *
import tables

class TestPost(unittest.TestCase):
  def test_path_add(self):
    self.json_dump = json        
    data = {
  "attributes": { 
    "device_id": "testdevice",
    "latitudes": [
      -27.269898, 
      -27.270928
    ], 
    "longitudes": [
      153.011927, 
      153.011906
    ], 
    "altitudes": [
      0.0, 
      0.0
    ],
    "name": "Example Path9"  
  }
}

    self.json_dump = json.dumps(data)
    url = 'http://127.0.0.1:5000/paths/new'
    x = requests.post(url,self.json_dump)

  # def test_poi_add(self):
  #     self.json_dump = json        
  #     data = {
  #   "attributes": { 
  #     "device_id": "testdevice",
  #     "latitude": [
  #       27.270000
  #     ], 
  #     "longitude": [
  #     153.012000
  #     ], 
  #     "name": "Example POI2"
  #   }
  # }

  #     self.json_dump = json.dumps(data)
  #     url = 'http://127.0.0.1:5000/pois/new'
  #     x = requests.post(url,self.json_dump)

  def delete_test_path(self):
      path_id = db.session.query_property.query(Path).count()
      data = {
    "attributes": { 
      "device_id": "testdevice"
    }
  }
      self.json_dump = json.dumps(data)
      url = 'http://127.0.0.1:5000/paths/<path_id>/delete'
      x = requests.post(url,self.json_dump)
    

if __name__ == '__main__':
    unittest.main()
