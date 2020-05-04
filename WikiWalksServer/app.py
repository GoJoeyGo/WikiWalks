from posts import *
from gets import *
from flask import Flask, render_template

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db.init_app(app)
ma.init_app(app)


app.register_blueprint(posts)
app.register_blueprint(gets)
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
    
