

$(document).ready(function() 
{ 
	new LivingScienceMenu();
	
	new PubList();
	new ExternalSearch();
	new AffiliationMap();
	new TopicGraph();
});


/**
 * -------------
 * MAIN MENU
 * -------------
 */

// provide url OR onclickfunc
function SubMenuEntry(name, urlStringOrFunction)
{
	this.name = name;
	this.urlStringOrFunction = urlStringOrFunction;
}

function MenuEntry(name, submenuEntriesList, mainMenu)
{
	this.name = name;
	this.submenuID = "subMenu" + name.split(' ').join('');;
	this.submenuEntriesList;
	this.isOpen = false;
	this.mainMenu = mainMenu;
	this.timeout = null;
	var that = this;
	
	var m = $("#lsMainMenu");
	
	this.$menuDiv = $("<div class=\"mainMenuEntry\">" + name + "</div>");
	m.append(this.$menuDiv);
	
	var $subMenuDiv = $("<div id=\"" + this.submenuID + "\" class=\"submenu\"/>");
	m.append($subMenuDiv);
	
	if (submenuEntriesList.length == 0)
	{
		this.$menuDiv.css("cursor", "pointer");
	}
	
	for (var i=0; i<submenuEntriesList.length; i++)
	{
		var $s = $("<a />");
		
		if (typeof(submenuEntriesList[i].urlStringOrFunction) == "string")
		{
			$s.attr("href", submenuEntriesList[i].urlStringOrFunction);
		}
		else
		{
			(function(f)
			{
				$s.click(function() 
				{
					f();
					that.hide();
				});
			})(submenuEntriesList[i].urlStringOrFunction);
		}
		$subMenuDiv.append($s);
		$s.append("<div class=\"menuListEntry\">" + submenuEntriesList[i].name + "</div>");
	}
	
	this.$menuDiv
	.mouseenter(function() 
	{
		for (var i=0; i<mainMenu.menuEntriesList.length; i++)
		{
			if (mainMenu.menuEntriesList[i] == that) continue;
			mainMenu.menuEntriesList[i].hide();
		}
		
		that.show();
	})
	.mouseleave(function() 
	{
		window.clearTimeout(that.timeout);
		that.timeout = window.setTimeout(function()
        {
            that.hide();
        }, 300);
	});
	
	$subMenuDiv
	.mouseenter(function() 
	{
		that.show();
	})	
	.mouseleave(function() 
	{
		window.clearTimeout(that.timeout);
		that.timeout = window.setTimeout(function()
        {
            that.hide();
        }, 300);
	});
	
	this.show = function()
	{
		window.clearTimeout(this.timeout);
		var left = that.$menuDiv.offset().left;
        $subMenuDiv.css("left", left + "px");
        $subMenuDiv.css("visibility", "visible");
	};
	
	this.hide = function()
	{
		$subMenuDiv.css("visibility", "hidden");
	};
}

function LivingScienceMenu()
{
	$("#mainLogo").click(function() 
	{
		   window.location = "/";
	});	
	
	
	this.menuEntriesList = new Array();
	
	var extMenu = typeof(data) != "undefined" ? true : false;
	
	var subMenuList = new Array();
	subMenuList.push(new SubMenuEntry("New Profile", function() 
	{ 
		var $content = $("<div>Feature not available.</div>");
		
		var buttons = [];
		
		var g = new GenericDialog("Create Profile", $content.get(0), buttons);
		g.$cancelButton.text("OK");
	}));
	subMenuList.push(new SubMenuEntry("New List", function() 
	{ 
		var $content = $("<div>Feature not available.</div>");
		
		var buttons = [];
		
		var g = new GenericDialog("Create List", $content.get(0), buttons);
		g.$cancelButton.text("OK");
	}));
	this.menuEntriesList.push(new MenuEntry("Create", subMenuList, this));
	
	if (extMenu)
	{
		var urlStart = "/" + (data.profile.isProfile ? "profiles" : "lists") + "/" + data.profile.lsid + "/";
		
		subMenuList = new Array();
		var editWhat = data.profile.isProfile ? "Profile" : "List";
		subMenuList.push(new SubMenuEntry("Modify " + editWhat, function() 
		{ 
			var $content = $("<div>Feature not available.</div>");
			
			var buttons = [];
			
			var g = new GenericDialog("Modify " + editWhat, $content.get(0), buttons);
			g.$cancelButton.text("OK");
		}));
		subMenuList.push(new SubMenuEntry("Remove " + editWhat, function() 
		{ 
			var $content = $("<div>Feature not available.</div>");
			
			var buttons = [];
			
			var g = new GenericDialog("Remove " + editWhat, $content.get(0), buttons);
			g.$cancelButton.text("OK");
		}));
		this.menuEntriesList.push(new MenuEntry("Edit", subMenuList, this));
		
		subMenuList = new Array();
		subMenuList.push(new SubMenuEntry("Add a Publication", function() 
		{ 
			var $content = $("<div>Feature not available.</div>");
			
			var buttons = [];
			
			var g = new GenericDialog("Add a Publication", $content.get(0), buttons);
			g.$cancelButton.text("OK");
		}));
//		subMenuList.push(new SubMenuEntry("Import from File", urlStart + "import"));
		subMenuList.push(new SubMenuEntry("Import from External Search", urlStart + "externalsearch/?q=" + data.profile.name + "&author=true&providers=a,x,m,p,s"));
		this.menuEntriesList.push(new MenuEntry("Add", subMenuList, this));

		subMenuList = new Array();
		subMenuList.push(new SubMenuEntry("List", urlStart, ""));
		subMenuList.push(new SubMenuEntry("Affiliation Map", urlStart + "affiliations"));
		subMenuList.push(new SubMenuEntry("Wikipedia Topics", urlStart + "topics"));
		subMenuList.push(new SubMenuEntry("Keyword Trends", urlStart + "ngrams"));	
		this.menuEntriesList.push(new MenuEntry("Analyze", subMenuList, this));
		
		subMenuList = new Array();
		subMenuList.push(new SubMenuEntry("Json", urlStart + "json"));
		subMenuList.push(new SubMenuEntry("Bibtex", urlStart + "bib"));
		this.menuEntriesList.push(new MenuEntry("Export", subMenuList, this));
	}
	
	subMenuList = new Array();
	var signInLabel = "Sign In";
//	if (typeof(login) != "undefined")
//	{
//		signInLabel = login.email;
//	}
	var signInMenu = new MenuEntry("" + signInLabel, subMenuList, this);
	this.menuEntriesList.push(signInMenu);
	signInMenu.$menuDiv.click(function() 
	{ 
		var $content = $("<div><div>No registration possible at the moment.</div></div>");
		
		var buttons = [];
		
		var g = new GenericDialog("Sign In", $content.get(0), buttons);
		g.$cancelButton.text("OK");
	});
}

/**
 * ---------------
 * Generic Form
 * ---------------
 */

function FormEntry(id, label, errorLabel)
{
	this.id = id;
	this.label = label;
	this.errorLabel = errorLabel;
}

function GenericForm(formEntryList)
{
	
}

/**
 * ---------------
 * Generic Dialog
 * ---------------
 */

function DialogButton(name, onclickfunc)
{
	this.name = name;
	this.onclickfunc = onclickfunc;
}

function GenericDialog(title, contentNode, dialogButtonList)
{
	var m = $("#alldialogs");
	
	var $backgroundLayer = $("<div style=\"position:absolute;left:0;top:0;right:0;bottom:0;opacity:0.4;background-color:rgb(0, 0, 0)\"><!-- _ --></div>");
	m.append($backgroundLayer);
	
	var cx = $backgroundLayer.width() / 2;
	var cy = $backgroundLayer.height() / 2 + $(window).scrollTop();

	
	// bottom:0 would only cover current view height
	var pageHeight = $(window).height();
	$backgroundLayer.css("height", pageHeight + "px");
	var $dialogDiv = $("<div style=\"position:absolute;\"/>");
	
	$dialogDiv.attr("class", "windowBg");
	m.append($dialogDiv);
	
	var $firstRow = $("<div style=\"width:100%\" />");
	$dialogDiv.append($firstRow);
	var $dialogHeader = $("<div style=\"float:left;\" class=\"windowTitle\">" + title + "</div>");
	$firstRow.append($dialogHeader);
	$firstRow.append($("<div style=\"height:30px;width:100%\"><!-- _ --></div>"));
	
	var $dialogContent = $("<div class=\"windowContent\"/>");
	$dialogDiv.append($dialogContent);
	$dialogContent.append(contentNode);
	
	var $buttons = $("<table style=\"float:right;\"/>");
	$buttons.attr("cellspacing", "0");
	$buttons.attr("cellpadding", "0");
	$dialogDiv.append($buttons);
	var $buttonsRow = $("<tr />");
	$buttons.append($buttonsRow);
	
	var that = this;
	
	for (var i=0; i<dialogButtonList.length; i++)
	{
		var $button = $("<td><div class=\"windowAction\">" + dialogButtonList[i].name + "</div></td>"); 
		$buttonsRow.append($button);
		
		(function(f)
		{
			$button.click(function() 
			{
				if (f()) that.close();
			});
		})(dialogButtonList[i].onclickfunc);

	}
	
	$cancelButtonEntry = $("<td />");
	this.$cancelButton = $("<div class=\"windowAction\">Cancel</div>"); 
	$cancelButtonEntry.append(this.$cancelButton);
	$buttonsRow.append($cancelButtonEntry);
	
	$cancelButtonEntry.click(function() 
	{
		that.close();
	});
	
	$backgroundLayer.click(function() 
	{
		that.close();
	});
	
	new DragObject($dialogDiv.get(0), $dialogHeader.get(0));
	
	
	this.close = function()
	{
		$dialogDiv.remove();
		$backgroundLayer.remove();
	};
	
	var dw = $dialogDiv.width();
	var dh = $dialogDiv.height();
	
	var left = cx - dw/2;
	var top = cy - dh/2;
	$dialogDiv.css("left", left + "px");
	$dialogDiv.css("top", top + "px");
}

/**
 * ---------------
 * Pub List
 * ---------------
 */

function getMetaString(pubMetaJSON)
{
	var meta = "";
	if (pubMetaJSON.authors && pubMetaJSON.authors.length > 0)
	{
		var displayedAuthors = Math.min(pubMetaJSON.authors.length, 10);
		if (pubMetaJSON.authors.length == 11) displayedAuthors = 9; 
		
		meta += pubMetaJSON.authors[0];
		for (var i=1; i<displayedAuthors; i++) meta += ", " + pubMetaJSON.authors[i];
		
		var leftAuthors = pubMetaJSON.authors.length - displayedAuthors;
		if (leftAuthors > 0)
		{
			meta += " ... and " + leftAuthors + " more authors";
		}
	}
	
	meta += " (" + pubMetaJSON.year + ")";
	if (pubMetaJSON.journal && pubMetaJSON.journal.length > 0) meta += " - " + pubMetaJSON.journal;
	return meta;
}

function PubRenderer(pubMetaJSON, parentJQuery)
{
	var id = pubMetaJSON.id;
	$pubContainer = $(
			"<div class=\"pubContainer\">" +
			"<table cellspacing=\"0\" cellpadding=\"0\">" +
			"<tr valign=\"top\">" +
			"<td>" +
			"<div class=\"pubLeft\">" +
				"<a id=\"title_" + id + "\" href=\"\" target=\"_blank\" class=\"pubLink\"></a>" +
				"<div id=\"meta_" + id + "\" class=\"pubMeta\"></div>" +
				"<div id=\"summary_" + id + "\" class=\"pubSummary pubSummary_short\"></div>" +
			"</div>" +
			"</td>" +
			"<td>" +
			"<div class=\"pubRight\" style=\"visibility: hidden;\">" +
				"<div id=\"edit_" + id + "\" class=\"pubAction pubButtonEdit\">Edit</div>" +
				"<div id=\"remove_" + id + "\" class=\"pubAction pubButtonRemove\">Remove</div>" +
				"<div id=\"merge_" + id + "\" class=\"pubAction pubButtonMerge\">Merge</div>" +
			"</div>" +
			"</td>" +
			"</tr>" +
			"</table>" +
			"</div>");
	parentJQuery.append($pubContainer);
	
	$("#title_" + id)
	.text(pubMetaJSON.title)
	.attr("href", pubMetaJSON.url)
	.click(function(event)
	{
		if($(this).attr('href') == '')
		{
			window.alert("DBLP does not provide a link.");
			event.preventDefault();
		}
	});
	
	if (pubMetaJSON.summary)
	{
		$("#summary_" + id).text(pubMetaJSON.summary);
	}
	

	$("#meta_" + id).text(getMetaString(pubMetaJSON));
	
	
	$pubContainer
	.mouseenter(function() 
	{
		$(this).find(".pubRight").css("visibility", "visible");
	})
	.mouseleave(function() 
	{
		$(this).find(".pubRight").css("visibility", "hidden");
	});
	
	$("#summary_" + id).click(function() 
	{
        $(this).toggleClass("pubSummary_short");
	});
			
	$("#edit_" + id).click(function() 
	{		
		var pubJsonObject = data.publications[id];
		var idInJson = pubJsonObject.id;
		delete pubJsonObject.id;
		var pubJsonString = JSON.stringify(pubJsonObject, undefined, 2);
		pubJsonObject.id = idInJson;
		
		var $content = $("<div />");
		var $textField = $("<textarea id=\"source\" rows=\"22\" cols=\"80\">" + pubJsonString + "</textarea>");
		$content.append($textField);
		var $result = $("<pre><!-- - --></pre>");
		$content.append($result);
		
		var validationFunc = function()
		{
			try
			{
		          var valResult = jsonlint.parse($textField.get(0).value);
		          if (valResult) 
		          {
		        	  $result.get(0).innerHTML = "Valid formatting.";
		        	  $result.attr("class", "editPubJsonPass");
		        	  $textField.get(0).value = JSON.stringify(valResult, undefined, 2);
		          }
		          return true;
			}
			catch (e)
			{
	        	  $result.get(0).innerHTML = e;
	        	  $result.attr("class", "editPubJsonFail");
			}
			return false;
		};
		
		var submitChanges = function(pubjson)
		{			
			console.log("submitChanges " + id);
			var url = "/pubs/" + id;
			var req = 
		    {
	            type: "POST",
	            url: url,
	            data: JSON.stringify(pubjson)
		    };
			var onPost = function()
			{
				console.log("post response for " + id);
				
				location.reload(true);
			};
			var onFail = function()
			{
				var $content = $("<div>Could not update this publication.</div>");
				var buttons = [];
				var g = new GenericDialog("Edit Publication", $content.get(0), buttons);
				g.$cancelButton.text("OK");
			};
			
			$.ajax(req).done(onPost).fail(onFail); 
		};
		
		var buttons = [
		   new DialogButton("Validate", function()
		   {
			   validationFunc();
			   return false;
		   }),
		   new DialogButton("Save", function()
		   {
			   var ok = validationFunc;
			   
			   if (ok)
			   {
				   submitChanges(jsonlint.parse($textField.get(0).value)); 
			   }
			   
			   return ok;
		   })];
		
		new GenericDialog("Edit Publication Metadata", $content.get(0), buttons);
	});
	
	$("#remove_" + id).click(function() 
	{
		var urlStart = "/" + (data.profile.isProfile ? "profiles" : "lists") + "/" + data.profile.lsid + "/";
		var url = urlStart + "pubs/" + id;
		var req = 
	    {
            type: "DELETE",
            url: url
	    };
		var onDelete = function()
		{
			location.reload(true);
		};
		var onFail = function()
		{
			var $content = $("<div>Could not remove this publication.</div>");
			var buttons = [];
			var g = new GenericDialog("Remove Publication", $content.get(0), buttons);
			g.$cancelButton.text("OK");
		};
		
		var $content = $("<div>Do you really want to remove this publication from this list?</div>");
		
		var buttons = [
		   new DialogButton("Remove", function()
		   {
			   $.ajax(req).done(onDelete).fail(onFail); 
			   return true;
		   })];
		    		
		 new GenericDialog("Remove Publication", $content.get(0), buttons);
	});
	
	$("#merge_" + id).click(function() 
	{
		var $content = $("<div><div>Feature not yet implemented.</div><div>In the meantime, you might remove the double entry manually</div><div>and update any relevant information for the other entry.</div></div>");
		var buttons = [];
		var g = new GenericDialog("Merge Publications", $content.get(0), buttons);
		g.$cancelButton.text("OK");
	});
}

function PubList()
{
	if ($("#pubList").length == 0) return;
	
	this.containsString = function(text, containingLowercaseString)
	{
		return text && text.toLowerCase().indexOf(containingLowercaseString) != -1;
	};
	
	var that = this;
	
	this.displayPubs = function(filterText)
	{
	
		var $pubs = $("#pubList");
		$pubs.empty();
		
		var haveFilterText = false;
		if (filterText)
		{
			filterText = $.trim(filterText.toLowerCase());
			if (filterText.length > 0) haveFilterText = true;
		}
		
		var pubArray = new Array();
		var total = 0;
		for (var id in data.publications)
	    {
			var pub = data.publications[id];
			total++;
			var add = false;
			
			if (haveFilterText)
			{
				if (that.containsString(pub.title, filterText) ||
					that.containsString(pub.journal, filterText) ||
					that.containsString("" + pub.year, filterText) ||
					that.containsString(pub.summary, filterText))
				{
					add = true;
				}
				else if (pub.authors)
				{
					for (var i=0; i<pub.authors.length; i++)
					{
						if (that.containsString(pub.authors[i], filterText))
						{
							add = true;
							break;
						}
					}
				}
			}
			else
			{
				add = true;
			}
			
			if (add) 
			{
				pubArray.push(pub);
			}
	    }
		if (that.setNoOfDisplayedPubs) that.setNoOfDisplayedPubs(pubArray.length, total);
		
		pubArray.sort(function(a,b)
		{
			return b.year - a.year;
		});
		for (var i=0; i<pubArray.length; i++)
		{
			new PubRenderer(pubArray[i], $pubs);
		}
	};
	
	this.displayPubs();
	
	this.addTimePeriod = function()
	{
		var years = new Array();
		for (var key in data.publications)
	    {
	         var year = data.publications[key].year; 
	         if (year && year > 0) years.push(year);
	    } 
		if (years.length == 0) return;
		
		var minYear = 3000;
		var maxYear = 0;
		
		for (var i=0; i<years.length; i++)
		{
			minYear = Math.min(years[i], minYear);
			maxYear = Math.max(years[i], maxYear);
		}

		if (minYear == maxYear) return;
		
		var yearCount = maxYear - minYear + 1;
		var yearCounts = new Array();
		for (var i=0; i<yearCount; i++) yearCounts[i] = 0;
		
		for (var i=0; i<years.length; i++)
		{
			yearCounts[years[i] - minYear]++;
		}
		
		
		var maxCount = 0;
		for (var i=0; i<yearCount; i++) maxCount = Math.max(yearCounts[i], maxCount);
		
		var svgText = "";
		for (var i=0; i<yearCount; i++)
		{
			var c = yearCounts[i];
			if (c == 0) continue;
			
			var x = 60 + (i * 100 / (yearCount-1)); // betwen 60 and 160
			var y = c * 50 / maxCount; // 0.. zero count, 50..maxCount
			
			svgText += "<path class=\"noOfPubsBar\" d=\"M" + x + " 56 L" + x + " " + (56 - y) + "\"/>";
		}
		
		
		$timeDiv = $("<div style=\"padding-top:24px\"></div>"); // <div style=\"padding-top:24px\">Time Period</div>
		
		$plot = $("<svg width=\"180px\" height=\"102px\" viewBox=\"0 0 180 102\" xmlns=\"http://www.w3.org/2000/svg\">" +
				"<defs><style type=\"text/css\">.text { font-weight:normal; font-family:arial,sans-serif; fill:rgb(64,64,64) } .textaxislabels { font-size:13px } .textticklabels { font-size:13px }  .plotAxisTicks { stroke:rgb(64,64,64); stroke-width:1px } .noOfPubsBar { fill:none; stroke:rgb(69,109,169); stroke-width:3px }</style></defs>" +
				"<g transform=\"translate(-12, 0)\">" +
				"<path d=\"M52.0 56.0 L52.0 6.0\" style=\"fill:none; stroke:rgb(64,64,64); stroke-width:1px\" />" +
				"<path d=\"M48.0 56.0 L56.0 56.0\" class=\"plotAxisTicks\"/>" +
				"<path d=\"M48.0 6.0 L56.0 6.0\" class=\"plotAxisTicks\"/>" +
				"<text x=\"44.0\" y=\"60\" text-anchor=\"end\" class=\"text textticklabels\">0</text>" +
				"<text x=\"44.0\" y=\"10\" text-anchor=\"end\" class=\"text textticklabels\">" + maxCount + "</text>" +
				"<text x=\"0\" y=\"0\" transform=\"translate(24, 31) rotate(-90)\" text-anchor=\"middle\" class=\"text textaxislabels\">#Papers</text>" +
				
				"<path d=\"M60.0 64.0 L160.0 64.0\" style=\"fill:none; stroke:rgb(64,64,64); stroke-width:1px\" />" +
				"<path d=\"M60.0 68.0 L60.0 60.0\" class=\"plotAxisTicks\"/>" +
				"<path d=\"M160.0 68.0 L160.0 60.0\" class=\"plotAxisTicks\"/>" +
				"<text x=\"60.0\" y=\"88\" text-anchor=\"middle\" class=\"text textticklabels\">" + minYear + "</text>" +
				"<text x=\"160.0\" y=\"88\" text-anchor=\"middle\" class=\"text textticklabels\">" + maxYear + "</text>" +
				"<text x=\"110\" y=\"100\" text-anchor=\"middle\" class=\"text textaxislabels\">Year</text>" +
				svgText +
				"</g></svg>");
		

		$timeDiv.append($plot);
		$("#searchOptions").append($timeDiv);
	};
	
	
	this.setNoOfDisplayedPubs = function(displayed, total)
	{
		that.$noOfDisplayedPubs.text(displayed + " of " +  total + " publications are displayed.");
	};
	
	this.addSearchFilter = function()
	{
		$searchDiv = $("<div><div style=\"padding-top:24px\">Search Filter</div>" +
		"</div>");
		
		$searchInput = $("<input autocomplete=\"off\" " +
		"style=\"margin-top:12px;background:rgb(244,243,240);padding-left: 1px;border-style: solid;border-color: black;border-width: 1px;font-family: arial, sans serif;padding-left: 1px;font-size:16px; width:160px\" " +
		"type=\"text\" name=\"searchFilter\" value=\"\"/>");
		$searchDiv.append($searchInput);
		
		

		that.$noOfDisplayedPubs = $("<div style=\"padding-top:12px\" />");
		$searchDiv.append(that.$noOfDisplayedPubs);
		var totalPubs = Object.keys(data.publications).length;
		var displayedPubs = totalPubs;
		that.setNoOfDisplayedPubs(displayedPubs, totalPubs);
		
		$("#searchOptions").append($searchDiv);
		
		$searchInput.change(function() 
		{
			that.displayPubs($(this).val());
		});
	};
	
	if (typeof(data) != "undefined" && $("#searchOptions").length > 0)
	{
		$("#searchOptions").append("<div style=\"padding-top:32px\"><!-- - --></div>");
		this.addTimePeriod();
		this.addSearchFilter();
	}
}

/**
 * ---------------
 * Affiliation Map
 * ---------------
 */

function ExternalSearch()
{
	if ($("#externalSearchSetup").length == 0) return;
	
	$.urlParam = function(name)
	{
		var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
		if (results == null) return null;
		return results[1] || 0;
	};
	
	var on = "checked=\"checked\"";
	var off = "";
	
	var googleScholar = off;
	var amazon = off;
	var arxiv = off;
	var plos = off;
	var mendeley = off;
	var springer = off;
	
	var p = $.urlParam("providers");
	if (p == null || p.length == 0)
	{
		googleScholar = off;
		amazon = off;
		arxiv = off;
		plos = off;
		mendeley = off;
		springer = off;
	}
	else
	{
		var ps = p.split(",");
		for (var i=0; i<ps.length; i++)
		{
			switch(ps[i])
			{
//			case "g": googleScholar = on; break;
			case "a": amazon = on; break;
			case "x": arxiv = on; break;
			case "p": plos = on; break;
			case "m": mendeley = on; break;
			case "s": springer = on; break;
			}
		}
	}
	
//	var selectAll = googleScholar == on && amazon == on && arxiv == on && plos == on && mendeley == on && springer == on ? on : off;
	
	var a = $.urlParam("author");
	var author = a == "true" ? on : off;
	var allFields = author == on ? off : on;
	
	$("#externalSearchSetup").append($("<div class=\"mainHeading\">Import from External Search</div>"));
	$contentEntry = $("<div class=\"contentEntry\" />");
	$("#externalSearchSetup").append($contentEntry);
	
	$searchform = $("<form id=\"extsearchform\" onsubmit=\"return false;\">" +
			"<table cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:13px\">" +
			"<tr><td><input " + googleScholar + " type=\"checkbox\" disabled=\"disabled\"  name=\"providers\" value=\"g\"> Google Scholar</input></td>" +
			"<td style=\"padding-left:20px\"><input " + arxiv + " type=\"checkbox\" name=\"providers\" value=\"x\"> ArXiv</input></td>" +
			"<td style=\"padding-left:20px\"><input " + mendeley + " type=\"checkbox\" name=\"providers\" value=\"m\"> Mendeley</input></td></tr>" +
			"<tr><td><input " + amazon + " type=\"checkbox\" name=\"providers\" value=\"a\"> Amazon</input></td>" +
			"<td style=\"padding-left:20px\"><input " + plos + " type=\"checkbox\" name=\"providers\" value=\"p\"> PLoS</input></td>" +
			"<td style=\"padding-left:20px\"><input " + springer + " type=\"checkbox\" name=\"providers\" value=\"s\"> Springer</input></td>" +
//			"<td style=\"padding-left:20px\"><input " + selectAll + " type=\"checkbox\" name=\"providers\" value=\"sa\"> Select All</input></div>" +
			"</tr>" +
			"<tr><td style=\"padding-top:15px\" colspan=\"2\"><input " + author + " type=\"radio\" name=\"author\" value=\"true\"> Search for an author</input></td>" +
			"<td style=\"padding-top:15px;padding-left:20px\" colspan=\"2\"><input type=\"radio\" " + allFields + " name=\"author\" value=\"false\"> Search in all fields</input></td></tr>"+
			"</table>" +
			"" +
			"<input type=\"text\" id=\"qext\" value=\"" + decodeURIComponent($.urlParam("q")) + "\" size=\"40\" name=\"qext\" style=\"margin-top:12px;background:rgb(244,243,240);padding-left: 1px;border-style: solid;border-color: black;border-width: 1px;font-family: arial, sans serif;padding-left: 1px;font-size:16px;\" autocomplete=\"off\">" +
			"" +
			"</form>");
	$contentEntry.append($searchform);
	
	$statusDiv = $("<div></div>");
	$contentEntry.append($statusDiv);
	
	this.noOfStatusChecks = 0;
	var that = this;
	
	this.submitsearch = function()
	{
	    that.providers = "" + $('input[name=providers]:checked', '#extsearchform').map(function(_, el) {
	        return $(el).val();
	    }).get();
	    that.q = encodeURIComponent($("#qext").val());
	    that.author = $('input[name=author]:checked', '#extsearchform').val();
	    that.url = "/profiles/" + data.profile.lsid + "/externalsearch/";
	    
	    that.req = 
	    {
            type: "GET",
            url: that.url,
            data: { q: that.q,
            		author: that.author,
            		providers: that.providers,
            		status: "true"}
	    };
	    
		
	    $statusDiv.empty();
	    $statusDiv.append($("<span>Waiting for results</span>"));
		
        $.ajax(that.req).done(that.onstatus);
        return false;
	};
	
	$searchform.submit(this.submitsearch); 
	
	this.onstatus = function(data)
	{
		that.noOfStatusChecks++;
    	if (data.status == "busy")
    	{
    		$statusDiv.append($("<span>.</span>"));
    		
    		if (that.noOfStatusChecks < 20)
    		{
    			setTimeout(function(){ $.ajax(that.req).done(that.onstatus); }, 1000);
    		}
    		else
    		{
    		    $statusDiv.empty();
    		    $statusDiv.append($("<span>Timeout for results. Please try again.</span>"));
    		    that.noOfStatusChecks = 0;
    		}
    	}
    	else if (data.status == "done")
    	{
    		window.location = that.url + "?q=" + that.q + "&author=" + that.author + "&providers="+ that.providers;
    	}
	};
	
	if ($("#externalSearchWait").length > 0)
	{
		this.submitsearch();
	}
	
}


/**
 * ---------------
 * Affiliation Map
 * ---------------
 */

function AffiliationMap_phase2()
{
	var win = document.getElementById("fullscreen");
	win.style.visibility = "visible";
	
	var mapOptions = {
			zoom: 3,
			center: new google.maps.LatLng(47.379022, 8.541001),
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			panControl: false,
			streetViewControl: false,
			mapTypeControl: false
	};

	var map = new google.maps.Map(win, mapOptions);
	
	var mapStyles = [
	 {
		 featureType: "road",
		 elementType: "all",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "transit",
		 elementType: "all",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "administrative.province",
		 elementType: "all",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "landscape",
		 elementType: "all",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "poi",
		 elementType: "all",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "water",
		 elementType: "labels",
		 stylers: [
		           { visibility: "off" }
		           ]
	 },
	 {
		 featureType: "administrative.locality", // cities
		 elementType: "labels",
		 stylers: [
		           { saturation: -100 },    
		           { lightness: 40 }
		           ]
	 },
	 {
		 featureType: "administrative.country",
		 elementType: "labels",
		 stylers: [		               
		           { lightness: 40 }
		           ]
	 },
	 {
		 featureType: "administrative.country",
		 elementType: "geometry",
		 stylers: [
		           { lightness: 40 }
		           ]
	 },
	 {
		 featureType: "water",
		 elementType: "geometry",
		 stylers: [
		           { saturation: -100 },
		           { lightness: 40 }
		           ]
	 }
	 ];
	
	map.setOptions({styles: mapStyles});


	var addWindow = function(city, cityCircle, latlng)
	{
		var unis = "";
		for (var i=0; i<city.institutions.length; i++)
		{
			unis += city.institutions[i];
			if (i < city.institutions.length - 1) unis += " | ";
		}
		
		var pubs = "";
		
		for (var i=0; i<city.pubs.length; i++)
		{
			var pubid = city.pubs[i];
			var pub = data.publications[pubid];
			
			pubs += '<div><a href="' + pub.url + '" target="_blank" class="pubLink">' + pub.title + '</a></div>' +
				'<div class="pubMeta">' + getMetaString(pub)  + '</div>';
		}

		var contentString = 
			'<div style="width:400px">' +
			'<div style="margin-bottom:12px">' + unis + '</div>' +
			'<div>' + pubs + '</div>' +
			'</div>';
		var infowindow = new google.maps.InfoWindow({
			content: contentString,
		});

		google.maps.event.addListener(cityCircle, 'click', function() {
			infowindow.setPosition(latlng);
			infowindow.open(map);
		});
	};
	  
	// add content
	for (var i=0; i<mapData.length; i++)
	{
		var city = mapData[i];
		
		var coords = new google.maps.LatLng(city.lat, city.lon);
	    var options = {
			      strokeColor: 'rgb(69,109,169)',
			      strokeOpacity: 0.8,
			      strokeWeight: 2,
			      fillColor: 'rgb(69,109,169)',
			      fillOpacity: 0.35,
			      map: map,
			      center: coords,
			      radius: 20000 * city.pubs.length
			    };
	    cityCircle = new google.maps.Circle(options);
	    addWindow(city, cityCircle, coords);

	}	
}

function AffiliationMap()
{
	var isMap = typeof(mapData) != "undefined" ? true : false;
	if (!isMap) return;
	
	var script = document.createElement("script");
	script.type = "text/javascript";
	script.src = "http://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&callback=AffiliationMap_phase2";
	document.body.appendChild(script);
}

/**
 * ---------------
 * Topic Graph
 * ---------------
 */

function TopicGraph()
{
	var isMap = typeof(graphData) != "undefined" ? true : false;
	if (!isMap) return;

	var win = document.getElementById("fullscreen");
	win.style.visibility = "visible";
	var width = $("#fullscreen").width();
	var height = $("#fullscreen").height();

	var force = d3.layout.force()
	.charge(-120)
	.linkDistance(60)
	.size([width, height]);

	var svg = d3.select(win).append("svg")
	.attr("width", width)
	.attr("height", height);

	svg.append("defs").append("style")
	.attr("type", "text/css")
	.text(".node {  } " +
			".pubcirclenode { stroke: rgb(96,96,96); stroke-width: 1.5px; fill:rgb(69,109,169); } " +
			".wikicirclenode { stroke: rgb(96,96,96); stroke-width: 1.5px; cursor:pointer; pointer-events: all; fill:rgb(162,161,160); } " +
			".wikicirclenode:hover { fill:rgb(69,109,169); } " +
			".link { stroke-width:1; stroke: rgb(162,161,160); stroke-opacity: .8; } " +
			"text { color:rgb(64,64,64); font-size:13px; font-family:arial,sans-serif; pointer-events: none; }");

	function onclicknode() 
	{
		var id = d3.select(this).attr("id");
		if (id.length > 0) window.open("http://en.wikipedia.org/wiki/" + id);
	}
	
	force
	.nodes(graphData.nodes)
	.links(graphData.links)
	.start();

	var link = svg.selectAll(".link")
	.data(graphData.links)
	.enter().append("line")
	.attr("class", "link");
//	.style("stroke-width", function(d) { return Math.sqrt(d.value); });

	var node = svg.selectAll(".node")
	.data(graphData.nodes)
	.enter().append("g")
	.attr("class", "node")
	.attr("id", function(d) { return d.group == 1 ? d.name : ""; })
	.on("click", onclicknode)
	.call(force.drag);
	
	node
	.append("circle")
	.attr("r", function(d) { return d.group == 2 ? 8 : 5; })
	.attr("class", function(d) { return d.group == 2 ? "pubcirclenode" : "wikicirclenode"; });
	
	node
	.append("text")
	.attr("x", 0)
	.attr("dy", 15)
	.attr("text-anchor", "middle")
	.text(function(d) { return d.group == 1 ? d.name : ""; });

	node.append("title")
	.text(function(d) { return d.group == 2 ? d.name : ""; });

	force.on("tick", function() {
		link.attr("x1", function(d) { return d.source.x; })
		.attr("y1", function(d) { return d.source.y; })
		.attr("x2", function(d) { return d.target.x; })
		.attr("y2", function(d) { return d.target.y; });

//		node.attr("cx", function(d) { return d.x; })
//		.attr("cy", function(d) { return d.y; });
		
		node
		.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	});
}

/**
 * ----------------
 * Draggable Window
 * ----------------
 */

function hookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.addEventListener)
  {
    element.addEventListener(eventName, callback, false);
  }
  else if(element.attachEvent)
    element.attachEvent("on" + eventName, callback);
}

function unhookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.removeEventListener)
    element.removeEventListener(eventName, callback, false);
  else if(element.detachEvent)
    element.detachEvent("on" + eventName, callback);
}

function cancelEvent(e)
{
  e = e ? e : window.event;
  if(e.stopPropagation)
    e.stopPropagation();
  if(e.preventDefault)
    e.preventDefault();
  e.cancelBubble = true;
  e.cancel = true;
  e.returnValue = false;
  return false;
}

function Position(x, y)
{
  this.X = x;
  this.Y = y;
  
  this.Add = function(val)
  {
    var newPos = new Position(this.X, this.Y);
    if(val != null)
    {
      if(!isNaN(val.X))
        newPos.X += val.X;
      if(!isNaN(val.Y))
        newPos.Y += val.Y;
    }
    return newPos;
  };
  
  this.Subtract = function(val)
  {
    var newPos = new Position(this.X, this.Y);
    if(val != null)
    {
      if(!isNaN(val.X))
        newPos.X -= val.X;
      if(!isNaN(val.Y))
        newPos.Y -= val.Y;
    }
    return newPos;
  };
  
  this.Min = function(val)
  {
    var newPos = new Position(this.X, this.Y);
    if(val == null)
      return newPos;
    
    if(!isNaN(val.X) && this.X > val.X)
      newPos.X = val.X;
    if(!isNaN(val.Y) && this.Y > val.Y)
      newPos.Y = val.Y;
    
    return newPos;  
  };
  
  this.Max = function(val)
  {
    var newPos = new Position(this.X, this.Y);
    if(val == null)
      return newPos;
    
    if(!isNaN(val.X) && this.X < val.X)
      newPos.X = val.X;
    if(!isNaN(val.Y) && this.Y < val.Y)
      newPos.Y = val.Y;
    
    return newPos;  
  };
  
  this.Bound = function(lower, upper)
  {
    var newPos = this.Max(lower);
    return newPos.Min(upper);
  };
  
  this.Check = function()
  {
    var newPos = new Position(this.X, this.Y);
    if(isNaN(newPos.X))
      newPos.X = 0;
    if(isNaN(newPos.Y))
      newPos.Y = 0;
    return newPos;
  };
  
  this.Apply = function(element)
  {
    if(typeof(element) == "string")
      element = document.getElementById(element);
    if(element == null)
      return;
    if(!isNaN(this.X))
      element.style.left = this.X + 'px';
    if(!isNaN(this.Y))
      element.style.top = this.Y + 'px';  
  };
}

function absoluteCursorPostion(eventObj)
{
  eventObj = eventObj ? eventObj : window.event;
  
  if(isNaN(window.scrollX))
    return new Position(eventObj.clientX + document.documentElement.scrollLeft + document.body.scrollLeft, 
      eventObj.clientY + document.documentElement.scrollTop + document.body.scrollTop);
  else
    return new Position(eventObj.clientX + window.scrollX, eventObj.clientY + window.scrollY);
}

function DragObject(element, attachElement, lowerBound, upperBound, startCallback, moveCallback, endCallback, attachLater, boundCallback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
      return;
  
  if(lowerBound != null && upperBound != null)
  {
    var temp = lowerBound.Min(upperBound);
    upperBound = lowerBound.Max(upperBound);
    lowerBound = temp;
  }

  var cursorStartPos = null;
  var elementStartPos = null;
  var dragging = false;
  var listening = false;
  var disposed = false;
  
  function dragStart(eventObj)
  { 
    if(dragging || !listening || disposed) return;
    dragging = true;
    
    if(startCallback != null)
      startCallback(eventObj, element);
    
    cursorStartPos = absoluteCursorPostion(eventObj);
    
    elementStartPos = new Position(parseInt(element.style.left), parseInt(element.style.top));
   
    elementStartPos = elementStartPos.Check();
    
    hookEvent(document, "mousemove", dragGo);
    hookEvent(document, "mouseup", dragStopHook);
    
    return cancelEvent(eventObj);
  }
  
  function dragGo(eventObj)
  {
    if(!dragging || disposed) return;
    
    var newPos = absoluteCursorPostion(eventObj);
    newPos = newPos.Add(elementStartPos).Subtract(cursorStartPos);
    newPos = newPos.Bound(lowerBound, upperBound);
    newPos.Apply(element);
    if(moveCallback != null)
      moveCallback(newPos, element);
        
    return cancelEvent(eventObj); 
  }
  
  function dragStopHook(eventObj)
  {
    dragStop();
    return cancelEvent(eventObj);
  }
  
  function dragStop()
  {
    if(!dragging || disposed) return;
    unhookEvent(document, "mousemove", dragGo);
    unhookEvent(document, "mouseup", dragStopHook);
    cursorStartPos = null;
    elementStartPos = null;
    if(endCallback != null)
      endCallback(element);
    dragging = false;
  }
  
  this.Dispose = function()
  {
    if(disposed) return;
    this.StopListening(true);
    element = null;
    attachElement = null;
    lowerBound = null;
    upperBound = null;
    startCallback = null;
    moveCallback = null;
    endCallback = null;
    disposed = true;
  };
  
  this.StartListening = function()
  {
    if(listening || disposed) return;
    listening = true;
    hookEvent(attachElement, "mousedown", dragStart);
  };
  
  this.StopListening = function(stopCurrentDragging)
  {
    if(!listening || disposed) return;
    unhookEvent(attachElement, "mousedown", dragStart);
    listening = false;
    
    if(stopCurrentDragging && dragging)
      dragStop();
  };
  
  this.IsDragging = function(){ return dragging; };
  this.IsListening = function() { return listening; };
  this.IsDisposed = function() { return disposed; };
  
  if(typeof(attachElement) == "string")
    attachElement = document.getElementById(attachElement);
  if(attachElement == null)
    attachElement = element;
    
  if(!attachLater)
    this.StartListening();
}
