from flask_marshmallow import Marshmallow
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


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        unknown = EXCLUDE
        include_fk = True
    points_of_interest = ma.Nested(PointOfInterestSchema, many=True)


class GroupWalkSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = GroupWalk
        unknown = EXCLUDE
    attendees = ma.Nested(UserSchema, exclude=["device_id"], many=True)


class PathPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathPicture
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])


class PointOfInterestPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestPicture
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])


class PathReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathReview
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])


class PointOfInterestReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestReview
        unknown = EXCLUDE
    submitter = ma.Nested(UserSchema, exclude=["device_id"])
