[app]
title = My Application
package.name = myapp
package.domain = org.example
source.dir = .
source.include_exts = py,png,jpg,kv,atlas
version = 0.1

requirements = python3==3.11,hostpython3==3.11,kivy,flask,jinja2,markupsafe,werkzeug,itsdangerous,click

orientation = portrait
fullscreen = 0
android.permissions = INTERNET
android.api = 33
android.minapi = 21
android.build_tools_version = 33.0.2
android.accept_sdk_license = True
