from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

group_walk_attendance = db.Table("group_walk_attendance", db.metadata,
                                 db.Column("user_id", db.Integer, db.ForeignKey("user.id"), primary_key=True),
                                 db.Column("group_walk", db.Integer, db.ForeignKey("group_walk.id"), primary_key=True))


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    device_id = db.Column(db.String, nullable=False, unique=True)
    nickname = db.Column(db.String, unique=True)
    paths = db.relationship("Path")
    points_of_interest = db.relationship("PointOfInterest")
    group_walks = db.relationship("GroupWalk")
    path_pictures = db.relationship("PathPicture")
    poi_pictures = db.relationship("PointOfInterestPicture")
    path_reviews = db.relationship("PathReview")
    poi_reviews = db.relationship("PointOfInterestReview")
    group_walks_attending = db.relationship("GroupWalk", secondary=group_walk_attendance,
                                            backref=db.backref("attendees", lazy=True))


class Path(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String, nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    children = db.relationship("Path")
    parent_path = db.Column(db.Integer, (db.ForeignKey("path.id")))
    latitudes = db.Column(db.JSON, nullable=False)
    longitudes = db.Column(db.JSON, nullable=False)
    walk_count = db.Column(db.Integer, default=1, nullable=False)
    starting_point = db.Column(db.JSON, nullable=False)
    ending_point = db.Column(db.JSON, nullable=False)
    points_of_interest = db.relationship("PointOfInterest")
    reviews = db.relationship("PathReview")
    pictures = db.relationship("PathPicture")


class PointOfInterest(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String, nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    path = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    reviews = db.relationship("PointOfInterestReview")
    pictures = db.relationship("PointOfInterestPicture")


class GroupWalk(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    time = db.Column(db.BigInteger)


class PathPicture(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    description = db.Column(db.String)
    url = db.Column(db.String, nullable=False)


class PointOfInterestPicture(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    point_of_interest_id = db.Column(db.Integer, db.ForeignKey("point_of_interest.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    description = db.Column(db.String)
    url = db.Column(db.String, nullable=False)


class PathReview(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    text = db.Column(db.String)
    rating = db.Column(db.Integer)


class PointOfInterestReview(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    point_of_interest_id = db.Column(db.Integer, db.ForeignKey("point_of_interest.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    text = db.Column(db.String)
    rating = db.Column(db.Integer)
