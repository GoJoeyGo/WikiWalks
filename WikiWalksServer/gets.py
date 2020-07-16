from datetime import datetime
from flask import jsonify, request, Blueprint, send_from_directory
from schemas import *

gets = Blueprint('gets_blueprint', __name__, template_folder='templates')


def get_submitter(device):
    user = User.query.filter_by(device_id=device).first()
    if user is None:
        new_user = User(device_id=device)
        db.session.add(new_user)
        db.session.commit()
        user = new_user
    return user


@gets.route("/paths/<path_id>", methods=["GET", "POST"])
def get_path(path_id):
    path = Path.query.get(path_id)
    if path is None:
        return jsonify({"error": "path not found"}), 404
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        if path in user.paths:
            path.editable = True
        pois = PointOfInterest.query.filter_by(path=path.id)
        for poi in pois:
            if poi in user.points_of_interest:
                poi.editable = True
        routes = Route.query.filter_by(path=path.id)
        for route in routes:
            if route in user.routes:
                route.editable = True
    path_schema = PathSchema()
    output = path_schema.dump(path)
    return jsonify({"path": output})


@gets.route("/paths/", methods=["GET", "POST"])
def get_paths():
    paths = Path.query.filter()
    north_boundary = request.args.get('n', default=90, type=float)
    south_boundary = request.args.get('s', default=-90, type=float)
    east_boundary = request.args.get('e', default=180, type=float)
    west_boundary = request.args.get('w', default=-180, type=float)
    in_range_paths = []
    for path in paths:
        if ((north_boundary >= path.boundaries[2] >= south_boundary or
             south_boundary <= path.boundaries[0] <= north_boundary or
             path.boundaries[0] < south_boundary < north_boundary < path.boundaries[2]) and
            (east_boundary >= path.boundaries[3] >= west_boundary or
             west_boundary <= path.boundaries[1] <= east_boundary or
             path.boundaries[1] < west_boundary < east_boundary < path.boundaries[3] or
             (east_boundary < west_boundary <= path.boundaries[3] <= 180 + (180 - east_boundary)))):
            in_range_paths.append(path)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for path in in_range_paths:
            if path in user.paths:
                path.editable = True
            pois = PointOfInterest.query.filter_by(path=path.id)
            for poi in pois:
                if poi in user.points_of_interest:
                    poi.editable = True
            routes = Route.query.filter_by(path=path.id)
            for route in routes:
                if route in user.routes:
                    route.editable = True
    path_schema = PathSchema(many=True)
    output = path_schema.dump(in_range_paths)
    return jsonify({"paths": output})


@gets.route("/paths/<path_id>/reviews", methods=["GET", "POST"])
def path_review_list(path_id):
    reviews = PathReview.query.filter_by(path_id=path_id)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for review in reviews:
            if review in user.path_reviews:
                review.editable = True
    for review in reviews:
        review_user = User.query.filter_by(id=review.submitter).first()
        review.submitter = review_user.nickname
    path_review_list_schema = PathReviewSchema(many=True)
    output = path_review_list_schema.dump(reviews)
    return jsonify({"reviews": output})


@gets.route("/paths/<path_id>/pictures", methods=["GET", "POST"])
def path_pictures_list(path_id):
    pictures = PathPicture.query.filter_by(path_id=path_id)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for picture in pictures:
            if picture in user.path_pictures:
                picture.editable = True
    path_picture_list_schema = PathPictureSchema(many=True)
    output = path_picture_list_schema.dump(pictures)
    return jsonify({"pictures": output})


@gets.route("/paths/<path_id>/group_walks", methods=["GET", "POST"])
def get_group_walks(path_id):
    group_walks = GroupWalk.query.filter_by(path_id=path_id)
    upcoming_group_walks = []
    for group_walk in group_walks:
        if group_walk.time > int(datetime.now().timestamp()):
            upcoming_group_walks.append(group_walk)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for group_walk in upcoming_group_walks:
            if group_walk in user.group_walks:
                group_walk.editable = True
    group_walks_list_schema = GroupWalkSchema(many=True)
    output = group_walks_list_schema.dump(upcoming_group_walks)
    return jsonify({"group_walks": output})


@gets.route("/pois/<poi_id>", methods=["GET", "POST"])
def get_poi(poi_id):
    poi = PointOfInterest.query.get(poi_id)
    if poi is None:
        return jsonify({"error": "poi not found"}), 404
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        if poi in user.points_of_interest:
            poi.editable = True
    poi_schema = PointOfInterestSchema()
    output = poi_schema.dump(poi)
    return jsonify({"poi": output})


@gets.route("/pois/", methods=["GET", "POST"])
def get_pois():
    pois = PointOfInterest.query.all()
    north_boundary = request.args.get('n', default=90, type=float)
    south_boundary = request.args.get('s', default=-90, type=float)
    east_boundary = request.args.get('e', default=180, type=float)
    west_boundary = request.args.get('w', default=-180, type=float)
    in_range_pois = []
    for poi in pois:
        lat = poi.latitude
        long = poi.longitude
        if north_boundary > lat > south_boundary and east_boundary > long > west_boundary:
            in_range_pois.append(poi)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for poi in in_range_pois:
            if poi in user.points_of_interest:
                poi.editable = True
    poi_schema = PointOfInterestSchema(many=True)
    output = poi_schema.dump(in_range_pois)
    return jsonify({"pois": output})


@gets.route("/pois/<poi_id>/reviews", methods=["GET", "POST"])
def poi_review_list(poi_id):
    reviews = PointOfInterestReview.query.filter_by(point_of_interest_id=poi_id)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for review in reviews:
            if review in user.poi_reviews:
                review.editable = True
    for review in reviews:
        review_user = User.query.filter_by(id=review.submitter).first()
        review.submitter = review_user.nickname
    poi_review_list_schema = PointOfInterestReviewSchema(many=True)
    output = poi_review_list_schema.dump(reviews)
    return jsonify({"reviews": output})


@gets.route("/pois/<poi_id>/pictures", methods=["GET", "POST"])
def poi_pictures_list(poi_id):
    pictures = PointOfInterestPicture.query.filter_by(point_of_interest_id=poi_id)
    if request.method == "POST":
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        for picture in pictures:
            if picture in user.poi_pictures:
                picture.editable = True
    poi_picture_list_schema = PointOfInterestPictureSchema(many=True)
    output = poi_picture_list_schema.dump(pictures)
    return jsonify({"pictures": output})


@gets.route("/pictures/<filename>", methods=["GET"])
def picture(filename):
    return send_from_directory("./images", filename)
