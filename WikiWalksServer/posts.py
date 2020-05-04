import json
from flask import Flask, jsonify, request, Blueprint
from schemas import *
import datetime

posts = Blueprint('posts_blueprint', __name__, template_folder='templates')


def get_submitter(device):
    user = User.query.filter_by(device_id=device).first()
    if user is None:
        new_user = User(device_id=device)
        db.session.add(new_user)
        db.session.commit()
        user = new_user
    return user


def get_time():
    time = int(datetime.datetime.now().timestamp())
    return time

	
@posts.route("/paths/new", methods=["POST"])
def add_path():
    try:
        path_schema = PathSchema()
        request_json = request.get_json(force=True)["attributes"]
        if len(request_json["latitudes"]) == len(request_json["longitudes"]) and len(request_json["latitudes"]) > 2:
            request_json["created_time"] = get_time()
            request_json["starting_point"] = [request_json["latitudes"][0], request_json["longitudes"][0]]
            request_json["ending_point"] = [request_json["latitudes"][len(request_json["latitudes"]) - 1],
                                            request_json["longitudes"][len(request_json["longitudes"]) - 1]]
            user = get_submitter(request_json["device_id"])
            data = path_schema.load(request_json)
            new_path = Path(**data, submitter=user.id, walk_count=1)
            db.session.add(new_path)
            if "parent_path" in request_json:
                parent_path = Path.query.get(request_json["parent_path"])
                parent_path.children.append(new_path)
            db.session.commit()
            return jsonify({"status": "success", "path": path_schema.dump(new_path)}), 201
        return jsonify({"status": "failed"}), 422
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/edit", methods=["POST"])
def edit_path(path_id):
    try:
        path_schema = PathSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path = Path.query.filter_by(id=path_id)
        if path.first() in user.paths:
            path.update(dict(path_schema.load(request_json, partial=True)))
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/delete", methods=["POST"])
def delete_path(path_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path = Path.query.get(path_id)
        if path in user.paths:
            db.session.delete(path)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/new", methods=["POST"])
def add_poi():
    try:
        poi_schema = PointOfInterestSchema()
        request_json = request.get_json(force=True)["attributes"]
        request_json["created_time"] = get_time()
        user = get_submitter(request_json["device_id"])
        data = poi_schema.load(request_json)
        new_poi = PointOfInterest(**data, submitter=user.id, path=request_json["path"])
        db.session.add(new_poi)
        db.session.commit()
        return jsonify({"status": "success", "poi": poi_schema.dump(new_poi)}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/edit", methods=["POST"])
def edit_poi(poi_id):
    try:
        poi_schema = PointOfInterestSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        poi = PointOfInterest.query.filter_by(id=poi_id)
        if poi.first() in user.points_of_interest:
            poi.update(dict(poi_schema.load(request_json, partial=True)))
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/delete", methods=["POST"])
def delete_point_of_interest(poi_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        poi = PointOfInterest.query.get(poi_id)
        if poi in user.points_of_interest:
            db.session.delete(poi)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/group_walks/new", methods=["POST"])
def add_group_walk():
    try:
        gw_schema = GroupWalkSchema()
        request_json = request.get_json(force=True)["attributes"]
        path = Path.query.filter_by(id=request_json["path_id"]).first()
        user = get_submitter(request_json["device_id"])
        new_gw = GroupWalk(submitter=user.id, created_time=get_time(), path_id=path.id, time=request_json["time"])
        db.session.add(new_gw)
        db.session.commit()
        return jsonify({"status": "success", "group_walk": gw_schema.dump(new_gw)}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/group_walks/<group_walk_id>/edit", methods=["POST"])
def edit_group_walk(group_walk_id):
    try:
        gw_schema = GroupWalkSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        group_walk = GroupWalk.query.filter_by(id=group_walk_id)
        if group_walk.first() in user.group_walks:
            group_walk.update(dict(gw_schema.load(request_json, partial=True)))
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/group_walks/<group_walk_id>/delete", methods=["POST"])
def delete_group_walk(group_walk_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        group_walk = GroupWalk.query.get(group_walk_id)
        if group_walk in user.group_walks:
            db.session.delete(group_walk)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/group_walks/<group_walk_id>/attend", methods=["POST"])
def toggle_group_walk_attendance(group_walk_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        group_walk = GroupWalk.query.get(group_walk_id)
        if user in group_walk.attendees:
            group_walk.attendees.remove(user)
        else:
            group_walk.attendees.append(user)
        db.session.commit()
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500
