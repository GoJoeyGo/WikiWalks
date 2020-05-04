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

@gets.route("/poi/<poi_id>", methods=["GET"])
def get_pointOfInterest(poi_id):
    poi = Path.query.get(poi_id)
    poi_schema = PointOfInterestSchema()
    output = poi_schema.dump(poi)
    return jsonify({"PointOfInterestPicture": output})
 

@gets.route("/group_walks/<group_walks_id>", methods=["GET"])
def get_groupWalk(group_walks_id):
    group_walks = Path.query.get(group_walks_id)
    group_walks_schema = GroupWalkSchema()
    output = group_walks_schema.dump(group_walks)
    return jsonify({"group_walks": output})

@gets.route("/PathPictureSchema/<path_picture_id>", methods=["GET"])
def get_pathPictureSchema(path_picture_id):
    path = Path.query.get(path_id)
    path_schema = PathPictureSchema()
    output = path_schema.dump(path)
    return jsonify({"path": output})




















'''@gets.route("/path/", methods=["GET"])
def get_paths():
    path = Path.query.order_by(Path.id).all()
    path_schema =PathSchema()
    idList = []
    output = []
    for x in path:
        temp = int(str(x).replace("Path","")[2::][:-1])
        idList.append(temp)
    out=[]
    for path_id in idList:
       get_path(path_id)
       p = path_schema.dump(path_id)
       out.append(p)
       print(path_id)
       #output.append(path_schema.dump(path_id))
    return jsonify({"path": out}) #"#jsonify({"path": str(output)}),
    '''
