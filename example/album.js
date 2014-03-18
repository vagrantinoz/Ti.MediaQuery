
function openPhotosByAlbum(e) {
	console.log("e.row._id :: " + e.row._id);
	
	var newWin = require("photosByAlbumId").createWindow({
		_id: e.row._id,
	});
	newWin.open();
}

function setThumbnail(wrapper, thumbnail_id) {
	setTimeout(function() {
		var thumbnail = require('com.tripvi.mediaquery').getThumbnail(thumbnail_id);
		console.log("thumbnail_id :: " + thumbnail_id)
		if (thumbnail) {
			console.log("thumbnail exist!!");
			var image = Ti.UI.createImageView({
				width: "75dp",
				height: (thumbnail.height * 75 / thumbnail.width) + "dp",
				image: typeof(thumbnail.image) == "string" ? "file://" + thumbnail.image : thumbnail.image,
			});
			wrapper.add(image);
		}
		else {
			console.log("thumbnail nothing!!");
		}
	}, 10);
}

function getPhotos(e) {
	
	var AndroidMediaQuery = require('com.tripvi.mediaquery');
	var win = e.source;

	var table = Ti.UI.createTableView();
	win.add(table);
	
	table.addEventListener("click", openPhotosByAlbum);
	table._release = function() {
		table.removeEventListener("click", openPhotosByAlbum);
		table = undefined;
	}
	
	var albums = AndroidMediaQuery.queryAlbumList();
	var rows = [];

	for (var i in albums) {
		
		var album = albums[i];
		
		console.log("orientation :: " + album["orientation"]);
		console.log(new Date(album["dateTaken"]));
		
		var row = Ti.UI.createTableViewRow({
			height: "75dp",
			_id: album.id,
		});
		
		var wrapper = Ti.UI.createView({
			width: Ti.UI.FILL, height: Ti.UI.FILL,
		});
		row.add(wrapper);
		
		var imageWrapper = Ti.UI.createView({
			left: 0,
			width: "75dp",
			height: "75dp",
		})
		wrapper.add(imageWrapper);
		
		setThumbnail(imageWrapper, albums[i]["thumbnail_id"]);
		
		var title = Ti.UI.createLabel({
			left: "85dp",
			text: album.name + " (" + album.photos_count + ")",
			font: {fontSize: "15dp", fontWeight: "bold"},
			color: "#000",
		});
		wrapper.add(title);
		
		rows.push(row);
	}

	table.setData(rows);
}

function onOpenWindow(e) {
	setTimeout(function() {
		getPhotos(e);
	}, 100);
}

function onCloseWindow(e) {
	
	for(var child in e.source.getChildren()) {
		if (child._release) {
			e.source.remove(child);
			child._release();
			child = undefined;
		}
	}
	
	e.source.removeEventListener("open", onOpenWindow);
	e.source.removeEventListener("close", onCloseWindow);
}

exports.createWindow = function() {
	
	var win = Ti.UI.createWindow({
		navBarHidden: true,
		backgroundColor: "#fff"
	})
	
	win.addEventListener("open", onOpenWindow);
	win.addEventListener("close", onCloseWindow);
	
	return win;
}