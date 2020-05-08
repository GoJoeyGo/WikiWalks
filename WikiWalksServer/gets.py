import json
from flask import Flask, jsonify, request, Blueprint
from schemas import *

gets = Blueprint('gets_blueprint', __name__, template_folder='templates')

    
@gets.route("/path/<path_id>", methods=["GET"])
def get_path(path_id):
    path = Path.query.get(path_id)
    path_schema = PathSchema()
    output = path_schema.dump(path)
    return jsonify({"path": output})
    
@gets.route("/path/", methods=["GET"])
def get_paths():
    path = Path.query.all()
    path_schema = PathSchema(many = True)
    output = path_schema.dump(path)
    return jsonify({"paths": output})
    
    
@gets.route("/poi/<poi_id>", methods=["GET"])
def get_pointOfInterest(poi_id):
    poi = Path.query.get(poi_id)
    poi_schema = PointOfInterestSchema()
    output = poi_schema.dump(poi)
    return jsonify({"PointOfInterest": output})
    
@gets.route("/poi/", methods=["GET"])
def get_pointOfInterests():
    poi = Path.query.query.all()
    poi_schema = PointOfInterestSchema(many = True)
    output = poi_schema.dump(poi)
    return jsonify({"PointOfInterests": output})

@gets.route("/group_walks/<group_walks_id>", methods=["GET"])
def get_groupWalk(group_walks_id):
    group_walks = Path.query.get(group_walks_id)
    group_walks_schema = GroupWalkSchema()
    output = group_walks_schema.dump(group_walks)
    return jsonify({"group_walk": output})
    
@gets.route("/group_walks/", methods=["GET"])
def get_groupWalks():
    group_walks = Path.query.all()
    group_walks_schema = GroupWalkSchema(many = True)
    output = group_walks_schema.dump(group_walks)
    return jsonify({"group_walks": output})
