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
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
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
    north_boundary = request.args.get('n', default=90, type=float)
    south_boundary = request.args.get('s', default=-90, type=float)
    east_boundary = request.args.get('e', default=180, type=float)
    west_boundary = request.args.get('w', default=-180, type=float)
    if abs(east_boundary - west_boundary) > 3 or abs(north_boundary - south_boundary) > 3:
        return jsonify({"status": "failed - distance too large"}), 500
    paths = Path.query.filter()
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
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for path in in_range_paths:
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
    path = Path.query.filter_by(id=path_id).first()
    page = request.args.get('page', default=1, type=int)
    reviews = PathReview.query.filter_by(path_id=path_id).order_by(PathReview.created_time.desc())
    own_review = None
    if request.method == "POST":
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for review in reviews:
            if review in user.path_reviews:
                review.editable = True
                if page == 1:
                    own_review = review
                reviews = reviews.filter(PathReview.submitter != user.id)
    for review in reviews:
        review_user = User.query.filter_by(id=review.submitter).first()
        review.submitter = review_user.nickname
    reviews = reviews.paginate(page, 10).items
    path_review_list_schema = PathReviewSchema(many=True)
    output = path_review_list_schema.dump(reviews)
    if own_review is None:
        return jsonify({"reviews": output, "average_rating": path.average_rating})
    else:
        return jsonify({"reviews": output, "own_review": path_review_list_schema.dump([own_review]),
                        "average_rating": path.average_rating})


@gets.route("/paths/<path_id>/pictures", methods=["GET", "POST"])
def path_pictures_list(path_id):
    page = request.args.get('page', default=1, type=int)
    pictures = PathPicture.query.filter_by(path_id=path_id).order_by(PathPicture.created_time.desc())
    if request.method == "POST":
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for picture in pictures:
            if picture in user.path_pictures:
                picture.editable = True
    for picture in pictures:
        picture_user = User.query.filter_by(id=picture.submitter).first()
        picture.submitter = picture_user.nickname
    pictures = pictures.paginate(page, 10).items
    path_picture_list_schema = PathPictureSchema(many=True)
    output = path_picture_list_schema.dump(pictures)
    return jsonify({"pictures": output})


@gets.route("/paths/<path_id>/group_walks", methods=["GET", "POST"])
def get_group_walks(path_id):
    group_walks = GroupWalk.query.filter_by(path_id=path_id)
    upcoming_group_walks = []
    for group_walk in group_walks:
        if group_walk.time > int(datetime.now().timestamp()):
            group_walk.submitter = User.query.filter_by(id=group_walk.submitter).first().nickname
            upcoming_group_walks.append(group_walk)
    if request.method == "POST":
        request_json = request.get_json(force=True)
        print(request.get_json(force=True))
        user = get_submitter(request_json["device_id"])
        for group_walk in upcoming_group_walks:
            if group_walk in user.group_walks:
                group_walk.editable = True
            if group_walk in user.group_walks_attending:
                group_walk.attending = True
    group_walks_list_schema = GroupWalkSchema(many=True)
    output = group_walks_list_schema.dump(upcoming_group_walks)
    return jsonify({"group_walks": output})


@gets.route("/pois/<poi_id>", methods=["GET", "POST"])
def get_poi(poi_id):
    poi = PointOfInterest.query.get(poi_id)
    if poi is None:
        return jsonify({"error": "poi not found"}), 404
    if request.method == "POST":
        request_json = request.get_json(force=True)
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
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for poi in in_range_pois:
            if poi in user.points_of_interest:
                poi.editable = True
    poi_schema = PointOfInterestSchema(many=True)
    output = poi_schema.dump(in_range_pois)
    return jsonify({"pois": output})


@gets.route("/pois/<poi_id>/reviews", methods=["GET", "POST"])
def poi_review_list(poi_id):
    point_of_interest = PointOfInterest.query.filter_by(id=poi_id).first()
    page = request.args.get('page', default=1, type=int)
    reviews = PointOfInterestReview.query.filter_by(point_of_interest_id=poi_id) \
        .order_by(PointOfInterestReview.created_time.desc())
    own_review = None
    if request.method == "POST":
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for review in reviews:
            if review in user.poi_reviews:
                review.editable = True
                if page == 1:
                    own_review = review
                reviews = reviews.filter(PointOfInterestReview.submitter != user.id)
    for review in reviews:
        review_user = User.query.filter_by(id=review.submitter).first()
        review.submitter = review_user.nickname
    reviews = reviews.paginate(page, 10).items
    poi_review_list_schema = PointOfInterestReviewSchema(many=True)
    output = poi_review_list_schema.dump(reviews)
    if own_review is None:
        return jsonify({"reviews": output, "average_rating": point_of_interest.average_rating})
    else:
        return jsonify({"reviews": output, "own_review": poi_review_list_schema.dump([own_review]),
                        "average_rating": point_of_interest.average_rating})


@gets.route("/pois/<poi_id>/pictures", methods=["GET", "POST"])
def poi_pictures_list(poi_id):
    page = request.args.get('page', default=1, type=int)
    pictures = PointOfInterestPicture.query.filter_by(point_of_interest_id=poi_id)\
        .order_by(PointOfInterestPicture.created_time.desc())
    if request.method == "POST":
        request_json = request.get_json(force=True)
        user = get_submitter(request_json["device_id"])
        for picture in pictures:
            if picture in user.poi_pictures:
                picture.editable = True
    for picture in pictures:
        picture_user = User.query.filter_by(id=picture.submitter).first()
        picture.submitter = picture_user.nickname
    pictures = pictures.paginate(page, 10).items
    poi_picture_list_schema = PointOfInterestPictureSchema(many=True)
    output = poi_picture_list_schema.dump(pictures)
    return jsonify({"pictures": output})


@gets.route("/pictures/<filename>", methods=["GET"])
def picture(filename):
    return send_from_directory("./images", filename)
