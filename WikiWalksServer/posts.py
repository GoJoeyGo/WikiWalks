import datetime
import os
import uuid

from PIL import Image, ImageOps
from flask import jsonify, request, Blueprint
from sqlalchemy import func

from schemas import *

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
posts = Blueprint('posts_blueprint', __name__, template_folder='templates')


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in {'png', 'jpg', 'jpeg'}


def process_picture(file):
    filename = str(uuid.uuid4()) + ".webp"
    image = Image.open(file.stream)
    image = ImageOps.exif_transpose(image)
    if image.mode in ("RGBA", "P"):
        image = image.convert("RGB")
    image.thumbnail((1920, 1920), Image.LANCZOS)
    return image, filename


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


@posts.route("/setname", methods=["POST"])
def set_name():
    try:
        user_schema = UserSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        user.nickname = request_json["name"]
        db.session.commit()
        return jsonify({"user": user_schema.dump(user)})
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/routes/new", methods=["POST"])
def add_route():
    try:
        path_schema = PathSchema()
        request_json = request.get_json(force=True)["attributes"]
        if len(request_json["latitudes"]) == len(request_json["longitudes"]) == len(request_json["altitudes"]) \
                and len(request_json["latitudes"]) > 0:
            user = get_submitter(request_json["device_id"])
            time = get_time()
            boundaries = [min(request_json["latitudes"]), min(request_json["longitudes"]),
                          max(request_json["latitudes"]), max(request_json["longitudes"])]
            if "path" in request_json:
                path = Path.query.filter_by(id=request_json["path"]).first()
                if path is not None:
                    current_boundaries = path.boundaries
                    path.boundaries = [min(current_boundaries[0], boundaries[0]),
                                       min(current_boundaries[1], boundaries[1]),
                                       max(current_boundaries[2], boundaries[2]),
                                       max(current_boundaries[3], boundaries[3])]
                else:
                    return jsonify({"status": "failed"}), 422
            else:
                marker_point = [request_json["latitudes"][0], request_json["longitudes"][0]]
                path = Path(name=request_json["name"], submitter=user.id, created_time=time, boundaries=boundaries,
                            marker_point=marker_point, walk_count=1, average_rating=0.0, rating_count=0)
                db.session.add(path)
                db.session.commit()
            new_route = Route(submitter=user.id, created_time=time, path=path.id, latitudes=request_json["latitudes"],
                              longitudes=request_json["longitudes"], altitudes=request_json["altitudes"])
            db.session.add(new_route)
            db.session.commit()
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
            return jsonify({"status": "success", "path": path_schema.dump(path)}), 201
        return jsonify({"status": "failed"}), 422
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/routes/<route_id>/delete", methods=["POST"])
def delete_route(route_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        route = Route.query.get(route_id)
        path = Path.query.filter_by(id=route.path).first()
        if route in user.routes:
            db.session.delete(route)
            routes = Route.query.filter_by(path=path.id)
            if routes.first() is not None:
                boundaries = [91, 181, -91, -181]
                for route in routes:
                    route_boundaries = [min(route.latitudes), min(route.longitudes), max(route.latitudes),
                                        max(route.longitudes)]
                    boundaries = [min(route_boundaries[0], boundaries[0]), min(route_boundaries[1], boundaries[1]),
                                  max(route_boundaries[2], boundaries[2]), max(route_boundaries[3], boundaries[3])]
                path.boundaries = boundaries
            else:
                db.session.delete(path)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/edit", methods=["POST"])
def edit_path(path_id):
    try:
        path_schema = PathSchema()
        request_json = request.get_json(force=True)["attributes"]
        path = Path.query.filter_by(id=path_id)
        path.update(dict(path_schema.load(request_json, partial=True)))
        db.session.commit()
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/walk", methods=["POST"])
def walk_path(path_id):
    try:
        path = Path.query.filter_by(id=path_id).first()
        path.walk_count = path.walk_count + 1
        db.session.commit()
        return jsonify({"status": "success", "new_count": path.walk_count}), 201
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
        poi = PointOfInterest.query.filter_by(id=poi_id)
        poi.update(dict(poi_schema.load(request_json, partial=True)))
        db.session.commit()
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


@posts.route("/paths/<path_id>/group_walks/new", methods=["POST"])
def add_group_walk(path_id):
    try:
        gw_schema = GroupWalkSchema()
        request_json = request.get_json(force=True)["attributes"]
        path = Path.query.filter_by(id=path_id).first()
        user = get_submitter(request_json["device_id"])
        new_gw = GroupWalk(submitter=user.id, created_time=get_time(), path_id=path.id, time=request_json["time"], title=request_json["title"])
        db.session.add(new_gw)
        db.session.commit()
        new_gw.submitter = user.nickname
        return jsonify({"status": "success", "group_walk": gw_schema.dump(new_gw)}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/group_walks/<group_walk_id>/edit", methods=["POST"])
def edit_group_walk(path_id, group_walk_id):
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


@posts.route("/paths/<path_id>/group_walks/<group_walk_id>/delete", methods=["POST"])
def delete_group_walk(path_id, group_walk_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        group_walk = GroupWalk.query.filter_by(id=group_walk_id).first()
        if group_walk in user.group_walks:
            db.session.delete(group_walk)
            db.session.commit()
        else:
            return jsonify({"status": "failed"}), 403
        return jsonify({"status": "success"}), 201
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/group_walks/<group_walk_id>/attend", methods=["POST"])
def toggle_group_walk_attendance(path_id, group_walk_id):
    try:
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        group_walk = GroupWalk.query.get(group_walk_id)
        gw_schema = GroupWalkSchema()
        if user in group_walk.attendees:
            group_walk.attendees.remove(user)
            attending = False
        else:
            group_walk.attendees.append(user)
            attending = True
        db.session.commit()
        group_walk.attending = attending
        return jsonify({"status": "success", "group_walk": gw_schema.dump(group_walk)}), 201
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
        point_of_interest = PointOfInterest.query.filter_by(id=poi_id).first()
        if point_of_interest.rating_count == 0:
            point_of_interest.average_rating = request_json["rating"]
            point_of_interest.rating_count = 1
        else:
            point_of_interest.average_rating = ((point_of_interest.average_rating * point_of_interest.rating_count) + request_json["rating"]) / \
                                  (point_of_interest.rating_count + 1)
            point_of_interest.rating_count = point_of_interest.rating_count + 1
        user = get_submitter(request_json["device_id"])
        data = poi_review_schema.load(request_json, partial=True)
        new_poi_review = PointOfInterestReview(**data, point_of_interest_id=poi_id, submitter=user.id, created_time=get_time())
        db.session.add(new_poi_review)
        db.session.commit()
        new_poi_review.submitter = user.nickname
        return jsonify({"status": "success", "review": poi_review_schema.dump(new_poi_review)}), 201
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
                poi.average_rating = db.session.query(func.avg(PointOfInterestReview.rating)).filter_by(
                    point_of_interest_id=poi.id).scalar()
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
        new_path_review.submitter = user.nickname
        return jsonify({"status": "success", "review": path_review_schema.dump(new_path_review)}), 201
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
        print(e)
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
            width, height = processed_image[0].size
            new_path_picture = PathPicture(path_id=path_id, submitter=user.id,
                                           created_time=get_time(), description=request.form["description"],
                                           url=processed_image[1], width=width, height=height)
            processed_image[0].save("./images/" + processed_image[1], 'WEBP', quality=80)
            db.session.add(new_path_picture)
            db.session.commit()
            return jsonify({"status": "success", "picture": path_picture_schema.dump(new_path_picture)}), 201
        else:
            return jsonify({"status": "invalid file type"}), 403
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/paths/<path_id>/pictures/<path_picture_id>/edit", methods=["POST"])
def edit_path_picture(path_id, path_picture_id):
    try:
        path_picture_schema = PathPictureSchema()
        request_json = request.get_json(force=True)["attributes"]
        user = get_submitter(request_json["device_id"])
        path_picture = PathPicture.query.filter_by(id=path_picture_id)
        if path_picture.first() in user.path_pictures:
            path_picture.update(dict(path_picture_schema.load(request_json, partial=True),
                                     url=path_picture.first().url))
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
            width, height = processed_image[0].size
            new_poi_picture = PointOfInterestPicture(point_of_interest_id=poi_id, submitter=user.id,
                                                     created_time=get_time(), description=request.form["description"],
                                                     url=processed_image[1], width=width, height=height)
            processed_image[0].save("./images/" + processed_image[1], 'WEBP', quality=80)
            db.session.add(new_poi_picture)
            db.session.commit()
            return jsonify({"status": "success", "picture": poi_picture_schema.dump(new_poi_picture)}), 201
        else:
            return jsonify({"status": "invalid file type"}), 403
    except Exception as e:
        print(e)
        return jsonify({"status": "failed"}), 500


@posts.route("/pois/<poi_id>/pictures/<poi_picture_id>/edit", methods=["POST"])
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
        if poi_picture in user.poi_pictures:
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
