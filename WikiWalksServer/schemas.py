from flask_marshmallow import Marshmallow, fields
from marshmallow import EXCLUDE

from tables import *

ma = Marshmallow()


class UserSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = User
        unknown = EXCLUDE


class PointOfInterestSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterest
        unknown = EXCLUDE
    editable = fields.fields.Boolean(default=False)


class RouteSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Route
        unknown = EXCLUDE
    editable = fields.fields.Boolean(default=False)


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        unknown = EXCLUDE
        include_fk = True
    routes = ma.Nested(RouteSchema, many=True)
    points_of_interest = ma.Nested(PointOfInterestSchema, many=True)
    editable = fields.fields.Boolean(default=False)


class GroupWalkSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = GroupWalk
        unknown = EXCLUDE
    attendees = ma.Nested(UserSchema, exclude=["device_id"], many=True)
    editable = fields.fields.Boolean(default=False)


class PathPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathPicture
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])
    editable = fields.fields.Boolean(default=False)


class PointOfInterestPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestPicture
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])
    editable = fields.fields.Boolean(default=False)


class PathReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathReview
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])
    editable = fields.fields.Boolean(default=False)


class PointOfInterestReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestReview
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])
    editable = fields.fields.Boolean(default=False)
