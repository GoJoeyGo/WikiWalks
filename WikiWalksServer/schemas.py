from flask_marshmallow import Marshmallow
from marshmallow import EXCLUDE

from tables import *

ma = Marshmallow()


class UserSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = User
        unknown = EXCLUDE


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        unknown = EXCLUDE


class PointOfInterestSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterest
        unknown = EXCLUDE


class GroupWalkSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = GroupWalk
        unknown = EXCLUDE


class PathPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathPicture
        unknown = EXCLUDE


class PointOfInterestPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestPicture
        unknown = EXCLUDE


class PathReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathReview
        unknown = EXCLUDE


class PointOfInterestReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestReview
        unknown = EXCLUDE
