from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

group_walk_attendance = db.Table("group_walk_attendance", db.metadata,
                                 db.Column("user_id", db.Integer, db.ForeignKey("user.id"), primary_key=True),
                                 db.Column("group_walk", db.Integer, db.ForeignKey("group_walk.id"), primary_key=True))


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    device_id = db.Column(db.String, nullable=False, unique=True)
    nickname = db.Column(db.String, unique=True)
    paths = db.relationship("Path", cascade="delete")
    routes = db.relationship("Route", cascade="delete")
    points_of_interest = db.relationship("PointOfInterest", cascade="delete")
    group_walks = db.relationship("GroupWalk", cascade="delete")
    path_pictures = db.relationship("PathPicture", cascade="delete")
    poi_pictures = db.relationship("PointOfInterestPicture", cascade="delete")
    path_reviews = db.relationship("PathReview", cascade="delete")
    poi_reviews = db.relationship("PointOfInterestReview", cascade="delete")
    group_walks_attending = db.relationship("GroupWalk", secondary=group_walk_attendance,
                                            backref=db.backref("attendees", lazy=True))

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class Path(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String, nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    routes = db.relationship("Route", cascade="delete")
    boundaries = db.Column(db.JSON, nullable=False)
    marker_point = db.Column(db.JSON, nullable=False)
    walk_count = db.Column(db.Integer, default=1, nullable=False)
    average_rating = db.Column(db.Float)
    rating_count = db.Column(db.Integer)
    points_of_interest = db.relationship("PointOfInterest", cascade="delete")
    reviews = db.relationship("PathReview", cascade="delete")
    pictures = db.relationship("PathPicture", cascade="delete")
    group_walks = db.relationship("GroupWalk", cascade="delete")
    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class Route(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    path = db.Column(db.Integer, (db.ForeignKey("path.id")))
    latitudes = db.Column(db.JSON, nullable=False)
    longitudes = db.Column(db.JSON, nullable=False)
    altitudes = db.Column(db.JSON, nullable=False)
    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class PointOfInterest(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String, nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    path = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    average_rating = db.Column(db.Float)
    rating_count = db.Column(db.Integer)
    reviews = db.relationship("PointOfInterestReview", cascade="delete")
    pictures = db.relationship("PointOfInterestPicture", cascade="delete")

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class GroupWalk(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    time = db.Column(db.Integer, nullable=False)

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class PathPicture(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    description = db.Column(db.String)
    url = db.Column(db.String, nullable=False)

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class PointOfInterestPicture(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    point_of_interest_id = db.Column(db.Integer, db.ForeignKey("point_of_interest.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    description = db.Column(db.String)
    url = db.Column(db.String, nullable=False)

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class PathReview(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    path_id = db.Column(db.Integer, db.ForeignKey("path.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    text = db.Column(db.String)
    rating = db.Column(db.Integer)

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }


class PointOfInterestReview(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    point_of_interest_id = db.Column(db.Integer, db.ForeignKey("point_of_interest.id"), nullable=False)
    submitter = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    created_time = db.Column(db.Integer, nullable=False)
    text = db.Column(db.String)
    rating = db.Column(db.Integer)

    __mapper_args__ = {
        'confirm_deleted_rows': False
    }

