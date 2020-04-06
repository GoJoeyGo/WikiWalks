from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///paths.db'
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
ma = Marshmallow(app)


class Path(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    picture = db.Column(db.String)
    latitude = db.Column(db.JSON)
    longitude = db.Column(db.JSON)
    rating = db.Column(db.Float)
    rating_count = db.Column(db.Integer)
    starting_point = db.Column(db.JSON)
    ending_point = db.Column(db.JSON)


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        include_fk = True


db.create_all()


@app.route("/paths/", methods=["GET"])
def path_list():
    paths = Path.query.all()
    print(paths)
    in_range_paths = []
    # for path in paths:
    #     lat = path.starting_point.get("latitude")
    #     print(lat)
    #     print("-27.269831")
    #     if lat == "-27.269831":
    #         in_range_paths.append(path)
    path_list_schema = PathSchema(many=True)
    output = path_list_schema.dump(paths).data
    return jsonify({"paths": output})


@app.route("/paths/<path_id>", methods=["GET"])
def path_detail(path_id):
    path = Path.query.get(path_id)
    path_schema = PathSchema()
    output = path_schema.dump(path).data
    return jsonify({"path": output})


if __name__ == '__main__':
    app.run(debug=True)
