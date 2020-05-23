import json

from flask import jsonify, request, Blueprint
from sqlalchemy import func

from schemas import *
import datetime
import os
from PIL import Image
import uuid

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
posts = Blueprint('posts_blueprint', __name__, template_folder='templates')


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in {'png', 'jpg', 'jpeg'}


def process_picture(file):
    filename = str(uuid.uuid1()) + ".jpg"
    image = Image.open(file.stream)
    if image.mode in ("RGBA", "P"):
        image = image.convert("RGB")
    image.thumbnail((1920, 1920), Image.LANCZOS)
    return image, filename


def update_boundaries(parent_id):
    parent = Path.query.filter_by(id=parent_id).first()
    parent.boundaries = [min(parent.latitudes), min(parent.longitudes), max(parent.latitudes), max(parent.longitudes)]
    for child in parent.children:
        parent.boundaries = [min(parent.boundaries[0], child.boundaries[0]),
                             min(parent.boundaries[1], child.boundaries[1]),
                             max(parent.boundaries[2], child.boundaries[2]),
                             max(parent.boundaries[3], child.boundaries[3])]
    db.session.commit()
    if parent.parent_path is not None:
        update_boundaries(parent.parent_path)


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
        if len(request_json["latitudes"]) == len(request_json["longitudes"]) == len(request_json["altitudes"]) \
                and len(request_json["latitudes"]) > 0:
            request_json["created_time"] = get_time()
            starting_point = [request_json["latitudes"][0], request_json["longitudes"][0]]
            ending_point = [request_json["latitudes"][len(request_json["latitudes"]) - 1],
                            request_json["longitudes"][len(request_json["longitudes"]) - 1]]
            boundaries = [min(request_json["latitudes"]), min(request_json["longitudes"]),
                          max(request_json["latitudes"]), max(request_json["longitudes"])]
            user = get_submitter(request_json["device_id"])
            data = path_schema.load(request_json, partial=True)
            new_path = Path(**data, submitter=user.id, walk_count=1, starting_point=starting_point,
                            ending_point=ending_point, boundaries=boundaries, rating_count=0, average_rating=0.0)
            db.session.add(new_path)
            db.session.commit()
            if "parent_path" in request_json:
                parent = Path.query.filter_by(id=request_json["parent_path"]).first()
                if parent is not None and request_json["parent_path"] is not new_path.id:
                    update_boundaries(request_json["parent_path"])
                    parent.children.append(new_path)
                else:
                    return jsonify({"status": "failed"}), 422
            return jsonify({"status": "success", "path": path_schema.dump(new_path)}), 201
        return jsonify({"status": "failed"}), 422
    except Exception as e:
        print(e.with_traceback())
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
            if path.parent_path is not None:
                update_boundaries(path.parent_path)
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
        new_poi = PointOfInterest(**data, submitter=user.id, path=request_json["path"], rating_count=0,
                                  average_rating=0.0)
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


@posts.route("/pois/<poi_id>/reviews/new", methods=["POST"])
def add_poi_review(poi_id):
    try:
        poi_review_schema = PointOfInterestReviewSchema()
        request_json = request.get_json(force=True)["attributes"]
        if request_json["rating"] > 5 or request_json["rating"] < 0:
            return jsonify({"status": "failed"}), 403
        poi = PointOfInterest.query.filter_by(id=poi_id).first()
        if poi.rating_count == 0:
            poi.average_rating = request_json["rating"]
            poi.rating_count = 1
        else:
            poi.average_rating = ((poi.average_rating * poi.rating_count) + request_json["rating"]) / \
                                 (poi.rating_count + 1)
            poi.rating_count = poi.rating_count + 1
        user = get_submitter(request_json["device_id"])
        data = poi_review_schema.load(request_json)
        new_poi_review = PointOfInterestReview(**data, point_of_interest_id=poi_id, submitter=user.id,
                                               created_time=get_time())
        db.session.add(new_poi_review)
        db.session.commit()
        return jsonify({"status": "success", "poi_review": poi_review_schema.dump(new_poi_review)}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/reviews/<poi_review_id>/edit", methods=["POST"])
def edit_poi_review(poi_id, poi_review_id):
    try:
        poi_review_schema = PointOfInterestReviewSchema()
        request_json = request.get_json(force=True)["attributes"]
        if request_json["rating"] > 5 or request_json["rating"] < 0:
            return jsonify({"status": "failed"}), 403
        user = get_submitter(request_json["device_id"])
        poi_review = PointOfInterestReview.query.filter_by(id=poi_review_id)
        if poi_review.first() in user.poi_reviews:
            poi = PointOfInterest.query.filter_by(id=poi_review.first().point_of_interest_id).first()
            poi_review.update(dict(poi_review_schema.load(request_json, partial=True)))
            poi.average_rating = db.session.query(func.avg(PointOfInterestReview.rating)).filter_by(
                point_of_interest_id=poi.id).scalar()
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/reviews/<poi_review_id>/delete", methods=["POST"])
def delete_poi_review(poi_id, poi_review_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        poi_review = PointOfInterestReview.query.get(poi_review_id)
        if poi_review in user.poi_reviews:
            poi = PointOfInterest.query.filter_by(id=poi_review.point_of_interest_id).first()
            db.session.delete(poi_review)
            poi.rating_count = poi.rating_count - 1
            if poi.rating_count != 0:
                poi.average_rating = db.session.query(func.avg(PointOfInterestReview.rating)).filter_by(point_of_interest_id=poi.id).scalar()
            else:
                poi.average_rating = 0
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/reviews/new", methods=["POST"])
def add_path_review(path_id):
    try:
        path_review_schema = PathReviewSchema()
        request_json = request.get_json(force=True)["attributes"]
        if request_json["rating"] > 5 or request_json["rating"] < 0:
            return jsonify({"status": "failed"}), 403
        path = Path.query.filter_by(id=path_id).first()
        if path.rating_count == 0:
            path.average_rating = request_json["rating"]
            path.rating_count = 1
        else:
            path.average_rating = ((path.average_rating * path.rating_count) + request_json["rating"]) / \
                                  (path.rating_count + 1)
            path.rating_count = path.rating_count + 1
        user = get_submitter(request_json["device_id"])
        data = path_review_schema.load(request_json, partial=True)
        new_path_review = PathReview(**data, path_id=path_id, submitter=user.id, created_time=get_time())
        db.session.add(new_path_review)
        db.session.commit()
        return jsonify({"status": "success", "path_review": path_review_schema.dump(new_path_review)}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/reviews/<path_review_id>/edit", methods=["POST"])
def edit_path_review(path_id, path_review_id):
    try:
        path_review_schema = PathReviewSchema()
        request_json = request.get_json(force=True)["attributes"]
        if request_json["rating"] > 5 or request_json["rating"] < 0:
            return jsonify({"status": "failed"}), 403
        user = get_submitter(request_json["device_id"])
        path_review = PathReview.query.filter_by(id=path_review_id)
        if path_review.first() in user.path_reviews:
            path = Path.query.filter_by(id=path_review.first().path_id).first()
            path_review.update(dict(path_review_schema.load(request_json, partial=True)))
            path.average_rating = db.session.query(func.avg(PathReview.rating)).filter_by(
                path_id=path.id).scalar()
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e.with_traceback())
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/reviews/<path_review_id>/delete", methods=["POST"])
def delete_path_review(path_id, path_review_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path_review = PathReview.query.get(path_review_id)
        if path_review in user.path_reviews:
            path = Path.query.filter_by(id=path_review.path_id).first()
            db.session.delete(path_review)
            path.rating_count = path.rating_count - 1
            if path.rating_count != 0:
                path.average_rating = db.session.query(func.avg(PathReview.rating)).filter_by(path_id=path.id).scalar()
            else:
                path.average_rating = 0
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/pictures/new", methods=["POST"])
def add_path_picture(path_id):
    try:
        if 'image' not in request.files:
            return jsonify({"status": "no file found"}), 500
        path_picture_schema = PathPictureSchema()
        image = request.files["image"]
        if allowed_file(image.filename):
            processed_image = process_picture(image)
            user = get_submitter(request.form["device_id"])
            new_path_picture = PathPicture(path_id=path_id, submitter=user.id, created_time=get_time(),
                                           description=request.form["description"], url=processed_image[1])
            image[0].save("./images/" + image[1], 'JPEG', quality=80)
            db.session.add(new_path_picture)
            db.session.commit()

            return jsonify({"status": "success", "path_picture": path_picture_schema.dump(new_path_picture)}), 201
        else:
            return jsonify({"status": "invalid file type"}), 403
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/reviews/<path_picture_id>/edit", methods=["POST"])
def edit_path_picture(path_id, path_picture_id):
    try:
        path_picture_schema = PathPictureSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path_picture = PathPicture.query.filter_by(id=path_picture_id)
        if path_picture.first() in user.path_pictures:
            path_picture.update(dict(path_picture_schema.load(request_json, partial=True), url=path_picture.url))
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/pictures/<path_picture_id>/delete", methods=["POST"])
def delete_path_picture(path_id, path_picture_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path_picture = PathPicture.query.get(path_picture_id)
        if path_picture in user.path_pictures:
            if os.path.isfile("./images/" + path_picture.url):
                os.remove("./images/" + path_picture.url)
            db.session.delete(path_picture)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/pictures/new", methods=["POST"])
def add_poi_picture(poi_id):
    try:
        if 'image' not in request.files:
            return jsonify({"status": "no file found"}), 500
        poi_picture_schema = PointOfInterestPictureSchema()
        image = request.files["image"]
        if allowed_file(image.filename):
            processed_image = process_picture(image)
            user = get_submitter(request.form["device_id"])
            new_poi_picture = PointOfInterestPicture(poi_id=poi_id, submitter=user.id, created_time=get_time(),
                                                     description=request.form["description"], url=processed_image[1])
            image[0].save("./images/" + image[1], 'JPEG', quality=80)
            db.session.add(new_poi_picture)
            db.session.commit()

            return jsonify({"status": "success", "poi_picture": poi_picture_schema.dump(new_poi_picture)}), 201
        else:
            return jsonify({"status": "invalid file type"}), 403
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<poi_id>/reviews/<poi_picture_id>/edit", methods=["POST"])
def edit_poi_picture(poi_id, poi_picture_id):
    try:
        poi_picture_schema = PointOfInterestPictureSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        poi_picture = PointOfInterestPicture.query.filter_by(id=poi_picture_id)
        if poi_picture.first() in user.path_pictures:
            poi_picture.update(dict(poi_picture_schema.load(request_json, partial=True), url=poi_picture.url))
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/pictures/<poi_picture_id>/delete", methods=["POST"])
def delete_poi_picture(poi_id, poi_picture_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        poi_picture = PointOfInterestPicture.query.get(poi_picture_id)
        if poi_picture in user.path_pictures:
            if os.path.isfile("./images/" + poi_picture.url):
                os.remove("./images/" + poi_picture.url)
            db.session.delete(poi_picture)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500
