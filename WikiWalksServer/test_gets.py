import requests
import json
import gets
import unittest
from flask import jsonify, request, Blueprint
from schemas import *
import tables




class TestGets(unittest.TestCase):
    def test_path(self):
        url = 'http://127.0.0.1:5000/paths/1'
        x = requests.get(url)
        self.assertEqual(x.json()["path"]["id"], 1, msg=None)

    def test_path_poi(self):
        url = 'http://127.0.0.1:5000/pois/1'
        x = requests.get(url)
        self.assertEqual(x.json()["poi"]["id"], 1, msg=None)
      

if __name__ == '__main__':
    unittest.main()


  
