import json

from flask import Flask, jsonify, request
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
    latitude = db.Column(db.JSON, nullable=False)
    longitude = db.Column(db.JSON, nullable=False)
    rating = db.Column(db.Float)
    rating_count = db.Column(db.Integer)
    starting_point = db.Column(db.JSON, nullable=False)
    ending_point = db.Column(db.JSON, nullable=False)


class PathSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Path
        include_fk = True


db.create_all()


@app.route("/paths/")
def path_list():
    paths = Path.query.all()
    print(paths)
    north_boundary = request.args.get('n', default=90, type=float)
    south_boundary = request.args.get('s', default=-90, type=float)
    east_boundary = request.args.get('e', default=180, type=float)
    west_boundary = request.args.get('w', default=-180, type=float)
    in_range_paths = []
    for path in paths:
        lat = path.starting_point[0]
        lat = path.starting_point[1]
        if lat == "-27.269831":
            in_range_paths.append(path)
    path_list_schema = PathSchema(many=True)
    output = path_list_schema.dump(paths).data
    return jsonify({"paths": output})


@app.route("/paths/<path_id>", methods=["GET"])
def path_detail(path_id):
    path = Path.query.get(path_id)
    path_schema = PathSchema()
    output = path_schema.dump(path).data
    return jsonify({"path": output})


@app.route("/new/path", methods=["POST"])
def add_path():
    try:
        path_schema = PathSchema()
        request_json = request.get_json(force=True)["attributes"]
        data = path_schema.loads(json.dumps(request_json)).data
        db.session.add(Path(**data))
        db.session.commit()
        return jsonify({"status": "success", "path": request_json})
    except:
        return jsonify({"status": "failed"})


if __name__ == '__main__':
    app.run(debug=True)