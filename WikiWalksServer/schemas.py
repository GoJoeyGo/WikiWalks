from flask_marshmallow import Marshmallow, Schema
from tables import *

ma = Marshmallow()


class UserSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = User
        exclude = ["device_id"]


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        exclude = ["submitter"]


class PointOfInterestSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterest
        exclude = ["submitter"]


class GroupWalkSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = GroupWalk
        exclude = ["submitter"]


class PathPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathPicture
        exclude = ["submitter"]


class PointOfInterestPictureSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestPicture
        exclude = ["submitter"]


class PathReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PathReview
        exclude = ["submitter"]


class PointOfInterestReviewSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = PointOfInterestReview
        exclude = ["submitter"]
