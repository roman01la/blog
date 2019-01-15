#!/bin/sh

if [ ! -f "build/closure-stylesheets.jar" ]
then
    echo "Downloading closure-stylesheets.jar..."
    curl -o build/closure-stylesheets.jar "https://github-production-release-asset-2e65be.s3.amazonaws.com/19253501/d4aaa168-6c58-11e7-8be8-a16172b0cfe5?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20190107%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20190107T165056Z&X-Amz-Expires=300&X-Amz-Signature=458d5923f56e390f11361b8b319109fcd555abbc4d4d0b6146503c1327f810f4&X-Amz-SignedHeaders=host&actor_id=1355501&response-content-disposition=attachment%3B%20filename%3Dclosure-stylesheets.jar&response-content-type=application%2Foctet-stream"
fi

echo "Cleaning..."

rm static/*.html
rm static/*.css

echo "Optimizing CSS..."
java -jar build/closure-stylesheets.jar resources/main.css --output-file main.min.css

# Cache-busting
hash=$(shasum -a 256 main.min.css)
css="main.${hash:0:8}.css"
mv main.min.css "static/${css}"

echo "Rendering site..."
clj -m blog.core --css $css

echo "Copying assets..."
cp -r resources/assets static/
cp -r resources/fonts static/

echo "Done!"
