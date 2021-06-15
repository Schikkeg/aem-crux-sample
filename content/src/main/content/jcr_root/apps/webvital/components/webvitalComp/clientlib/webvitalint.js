(function($, $document){
    $document.on("cq-page-info-loaded", fetchWebVital);

	function initAcc(elem, option){
    	document.addEventListener('click', function (e) {
        if (!e.target.matches(elem+' .a-btn')) return;
        else{
            if(!e.target.parentElement.classList.contains('active')){
                if(option==true){
                    var elementList = document.querySelectorAll(elem+' .a-container');
                    Array.prototype.forEach.call(elementList, function (e) {
                        e.classList.remove('active');
                    });
                }            
                e.target.parentElement.classList.add('active');
            }else{
                e.target.parentElement.classList.remove('active');
            }
        }
    });
	}

    function fetchWebVital(){
			var pagePath = Granite.author.ContentFrame.currentLocation().replace(".html","");
            $.ajax({
                type: "GET",
                url: Granite.HTTP.externalize("/apps/public/webvital?path="+pagePath)
            }).done(function(data) {
                if(	data.statusmessage !== undefined ){
					var desktopWebVital = '',mobileWebVital='',tabletWebVital='',mobile3gWebVital='',mobile4gWebVital='',allWebVital='';
					var jsonwvobj = JSON.parse(data.statusmessage.replaceAll("'","\""));
					var firstOne = false;
					var showMore = false;
					var moreAdded = false;
					var moreText = '<div class="more"><button class="btn-dropdown toggle" id="btn-dropdown" data-target="webData"><i class="coral-Icon coral-Icon--chevronDown coral-Icon--sizeXS"></i></button></div> <div class="web-vitals-data" id="webData"><div class="web-vital-row"><div class="web-vitals-col fullWidth-col">' ;
					if(JSON.stringify(jsonwvobj.PHONE) !== JSON.stringify({})){
						mobileWebVital = ' <div class="deviceScores"><div class="label">Mobile</div><div class="scores"><span class="span-row score-info"><span>CLS:&nbsp;</span><span class="'+jsonwvobj.PHONE.CLSSTYLE+'">'+jsonwvobj.PHONE.CLS+'</span><span class="tooltiptext">'+jsonwvobj.PHONE.CLSTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;FID:&nbsp;</span><span class="'+jsonwvobj.PHONE.FIDSTYLE+'">'+jsonwvobj.PHONE.FID+'</span><span class="tooltiptext">'+jsonwvobj.PHONE.FIDTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;LCP:&nbsp;</span><span class="'+jsonwvobj.PHONE.LCPTYLE+'">'+jsonwvobj.PHONE.LCP+'</span><span class="tooltiptext">'+jsonwvobj.PHONE.LCPTOOLTIP+'</span></span></div></div>';
					 	allWebVital = '<div class="webvitals">' +mobileWebVital;
						firstOne = true;
                     }
					
                    if(JSON.stringify(jsonwvobj.DESKTOP) !== JSON.stringify({})){
						desktopWebVital = ' <div class="deviceScores"><div class="label">Desktop</div><div class="scores"><span class="span-row score-info"><span>CLS:&nbsp;</span><span class="'+jsonwvobj.DESKTOP.CLSSTYLE+'">'+jsonwvobj.DESKTOP.CLS+'</span><span class="tooltiptext">'+jsonwvobj.DESKTOP.CLSTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;FID:&nbsp;</span><span class="'+jsonwvobj.DESKTOP.FIDSTYLE+'">'+jsonwvobj.DESKTOP.FID+'</span><span class="tooltiptext">'+jsonwvobj.DESKTOP.FIDTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;LCP:&nbsp;</span><span class="'+jsonwvobj.DESKTOP.LCPTYLE+'">'+jsonwvobj.DESKTOP.LCP+'</span><span class="tooltiptext">'+jsonwvobj.DESKTOP.LCPTOOLTIP+'</span></span></div></div>';
						if(firstOne){
							if(!showMore){
								if(moreAdded){
									allWebVital = allWebVital  + '<div class="horizontal-card">'+ desktopWebVital +'</div><hr>';
								}else{
									allWebVital = allWebVital + moreText + '<div class="horizontal-card">'+ desktopWebVital +'</div><hr>';
									moreAdded = true;
								}
							}else{
									allWebVital = allWebVital  + '<div class="horizontal-card">'+ desktopWebVital +'</div><hr>';
							}
							showMore = true;
						}else{
							firstOne = true;
							allWebVital = '<div class="webvitals">' +desktopWebVital;
						}
					}
					if(JSON.stringify(jsonwvobj.TABLET) !== JSON.stringify({})){
						tabletWebVital = ' <div class="deviceScores"><div class="label">Tablet</div><div class="scores"><span class="span-row score-info"><span>CLS:&nbsp;</span><span class="'+jsonwvobj.TABLET.CLSSTYLE+'">'+jsonwvobj.TABLET.CLS+'</span><span class="tooltiptext">'+jsonwvobj.TABLET.CLSTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;FID:&nbsp;</span><span class="'+jsonwvobj.TABLET.FIDSTYLE+'">'+jsonwvobj.TABLET.FID+'</span><span class="tooltiptext">'+jsonwvobj.TABLET.FIDTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;LCP:&nbsp;</span><span class="'+jsonwvobj.TABLET.LCPTYLE+'">'+jsonwvobj.TABLET.LCP+'</span><span class="tooltiptext">'+jsonwvobj.TABLET.LCPTOOLTIP+'</span></span></div></div>';
						if(firstOne){
							if(!showMore){
								if(moreAdded){
									allWebVital = allWebVital + '<div class="horizontal-card">'+ tabletWebVital +'</div><hr>';
								}else{
									allWebVital = allWebVital + moreText + '<div class="horizontal-card">'+ tabletWebVital +'</div><hr>';
									moreAdded = true;
								}
							}else{
								allWebVital = allWebVital + '<div class="horizontal-card">'+ tabletWebVital +'</div><hr>';
							}
							showMore = true;
						}else{
							firstOne = true;
							allWebVital = '<div class="webvitals">' +tabletWebVital;
						}
					}                   
                    if(JSON.stringify(jsonwvobj.PHONE3G) !== JSON.stringify({})){
						mobile3gWebVital = ' <div class="deviceScores"><div class="label">PHONE3G</div><div class="scores"><span class="span-row score-info"><span>CLS:&nbsp;</span><span class="'+jsonwvobj.PHONE3G.CLSSTYLE+'">'+jsonwvobj.PHONE3G.CLS+'</span><span class="tooltiptext">'+jsonwvobj.PHONE3G.CLSTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;FID:&nbsp;</span><span class="'+jsonwvobj.PHONE3G.FIDSTYLE+'">'+jsonwvobj.PHONE3G.FID+'</span><span class="tooltiptext">'+jsonwvobj.PHONE3G.FIDTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;LCP:&nbsp;</span><span class="'+jsonwvobj.PHONE3G.LCPTYLE+'">'+jsonwvobj.PHONE3G.LCP+'</span><span class="tooltiptext">'+jsonwvobj.PHONE3G.LCPTOOLTIP+'</span></span></div></div>';
						if(firstOne){
							if(!showMore){
								if(moreAdded){
									allWebVital = allWebVital + '<div class="horizontal-card">'+ mobile3gWebVital +'</div><hr>';
								}else{
									allWebVital = allWebVital + moreText + '<div class="horizontal-card">'+ mobile3gWebVital+'</div><hr>';
									moreAdded = true;
								}
							}else{
								allWebVital = allWebVital + '<div class="horizontal-card">'+ mobile3gWebVital +'</div><hr>';
							}
							showMore = true;
						}else{
							firstOne = true;
							allWebVital = '<div class="webvitals">' +mobile3gWebVital;
						}
					} 
                    if(JSON.stringify(jsonwvobj.PHONE4G) !== JSON.stringify({})){
						mobile4gWebVital = ' <div class="deviceScores"><div class="label">PHONE4G</div><div class="scores"><span class="span-row score-info"><span>CLS:&nbsp;</span><span class="'+jsonwvobj.PHONE4G.CLSSTYLE+'">'+jsonwvobj.PHONE4G.CLS+'</span><span class="tooltiptext">'+jsonwvobj.PHONE4G.CLSTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;FID:&nbsp;</span><span class="'+jsonwvobj.PHONE4G.FIDSTYLE+'">'+jsonwvobj.PHONE4G.FID+'</span><span class="tooltiptext">'+jsonwvobj.PHONE4G.FIDTOOLTIP+'</span></span><span class="span-row score-info"><span>&nbsp;LCP:&nbsp;</span><span class="'+jsonwvobj.PHONE4G.LCPTYLE+'">'+jsonwvobj.PHONE4G.LCP+'</span><span class="tooltiptext">'+jsonwvobj.PHONE4G.LCPTOOLTIP+'</span></span></div></div>';
						if(firstOne){
							if(!showMore){
								if(moreAdded){
									allWebVital = allWebVital + '<div class="horizontal-card">'+ mobile4gWebVital  +'</div><hr>';
								}else{
									allWebVital = allWebVital + moreText + '<div class="horizontal-card">'+ mobile4gWebVital  +'</div><hr>';
									moreAdded = true;
								}
							}else{
								allWebVital = allWebVital + '<div class="horizontal-card">'+ mobile4gWebVital  +'</div><hr>';
							}
							showMore = true;
						}else{
							firstOne = true;
							allWebVital = '<div class="webvitals">' +mobile4gWebVital;
						}
					} 	

					if(moreAdded){
						allWebVital = allWebVital + '   </div></div>' ;
					}

					if(!firstOne){
						allWebVital = '<div class="webvitals"><div class="deviceScores"></div>'+moreText  + '   </div></div>';
                    }else if(!moreAdded){
						allWebVital = allWebVital +moreText+ '   </div></div>' ;
                    }
					// Reference/labdata/Akamai Integration 
					//pending to add condition for null reference more
					
					
					if(JSON.stringify(jsonwvobj.ORIGINS) !== JSON.stringify({})){
						allWebVital = allWebVital +'<div class="reference-sites"><h2 class="heading-block">Reference sites</h2><div class="accordion v2"> ';
						var originVital = jsonwvobj.ORIGINS ;
						for(var counter=0;counter<originVital.length;counter++){
                            var originObj = originVital[counter];
							allWebVital = allWebVital +'<div class="a-container"><p class="a-btn">'+originObj.name+' <span></span></p><div class="a-panel">';
							var hrflag = false;
							var innerCounter = 0;
                            for (key in originObj){
                            	++innerCounter;
                                if(key=="name" || (JSON.stringify(originObj[key]) == JSON.stringify({}))){
									continue;
                                }
                                var len = Object.keys(originObj).length;
                           		var classTop = "";
                            	if(innerCounter==len){
									classTop = " top";
                            	}
                                if(hrflag){
									allWebVital = allWebVital +'<hr>';
                                }
								allWebVital = allWebVital +'<div class="horizontal-card"><div class="deviceScores"><div class="label">'+key+'</div><div class="scores">';
								var displayScore = originObj[key];
                                allWebVital = allWebVital + ' <span class="span-row score-info'+classTop+'"><span>CLS:&nbsp;</span><span class="'+displayScore.CLSSTYLE+'">'+displayScore.CLS+'</span><span class="tooltiptext">'+displayScore.CLSTOOLTIP+'</span></span><span class="span-row score-info'+classTop+'"><span>&nbsp;FID:&nbsp;</span><span class="'+displayScore.FIDSTYLE+'">'+displayScore.FID+'</span><span class="tooltiptext">'+displayScore.FIDTOOLTIP+'</span></span><span class="span-row score-info'+classTop+'"><span>&nbsp;LCP:&nbsp;</span><span class="'+displayScore.LCPTYLE+'">'+displayScore.LCP+'</span><span class="tooltiptext">'+displayScore.LCPTOOLTIP+'</span></span></div></div>';
                                allWebVital = allWebVital +'</div>';

                            }
							allWebVital = allWebVital +'</div></div>';
						}
						
						allWebVital = allWebVital +'</div></div>';
						
						
					}


					if(moreAdded){
						allWebVital = allWebVital + '   </div></div>' ;
					}else{
						allWebVital = allWebVital + '   </div>' ;
					}
					

					
					
                   var webVitals = allWebVital +'<style>   .webvitals{position:absolute;z-index:1;top:5px;width:330px;left:100px;text-align:center;text-transform:uppercase;color:#a0a0a0;display:flex;justify-content:center;}   .deviceScores,.mobileScore{display:flex;flex-direction:column;padding:0 10px;position:relative}   .scores{font-weight:700}   .scores span:nth-child(even){margin-right:5px;}   .fail{color:#ff4e42}   .average{color:#ffa400}   .pass{color:#0cce6b}   .web-vitals-data{display:none;position:absolute;top:39px;left:0;background:#2c2b2b;width:85%;height:calc(100vh - 100px);overflow-y:scroll;transition:all 0.3s ease-out;}   .web-vitals-data.open{display:block;}    .web-vitals-data hr{border:0px solid;border-bottom:1px solid #9e9e9e;}   .label{margin-bottom:3px;}   .fullWidth-col{flex-wrap: wrap;display: flex;width: 100%;}   .fullWidth-col > hr{display: none;}   .fullWidth-col .horizontal-card{padding-bottom:5px;padding-top:5px;flex: 0 0 100%;max-width: 100%; border-bottom: 1px solid #ccc;} .horizontal-card{padding:7px 0;} .horizontal-card .deviceScores .label{margin-bottom:3px;}     .deviceScores .label{margin-top:0px;margin-bottom:0px;color:#a0a0a0;font-weight:600;}   .deviceScores .score-bars{padding:0.5rem 0;background:#fff;border-bottom:1px solid;display:flex;justify-content:space-between;}   .deviceScores .score-bars .label{display:flex;justify-content:space-around;width:100%;}   .more{margin-top:-5px;z-index:1}   .more button{background:transparent;-webkit-appearance:transparent;height:44px;min-width:44px;text-align:center;vertical-align:top;color:#a0a0a0;box-shadow:0px;cursor:pointer!important;border-width:0;border-color:transparent;}   .more button:focus{outline:0;-webkit-box-shadow:0 0 0.1875rem #326ec8;box-shadow:0 0 0.1875rem #326ec8;border:.0625rem solid #326ec8;}   .more button:hover i{color:#fff;}   .more button:focus i{color:#fff;}   .web-vital-row{display:flex;justify-content:space-between;}   .web-vital-row .web-vitals-col:not(.fullWidth-col){border-right:1px solid #cecece;padding-right:10px;}  .web-vitals-col:not(.fullWidth-col){flex:0 0 48%;max-width:48%}    .all-bowsers-stats .web-vital-row{flex-direction: column;}   .all-bowsers-stats .web-vitals-col-3{flex:0 0 33.33%;max-width:33.335;}   .all-bowsers-stats .score-block{font-weight:600;padding:1rem 0;border-bottom:1px solid #ccc}   .all-bowsers-stats .score-block .score{font-size:18px;margin-top:15px;}   .all-bowsers-stats .score-block .score sub{bottom:-0.013em;text-transform:capitalize;}   .container--tabs{margin:1rem 0;width:100%;}   .container--tabs .container-row{display:flex;justify-content:flex-start;}   .container--tabs .container-row .webVitals-tabs-nav{display:flex;justify-content:flex-start;width:40%}  .container--tabs .webVitals-tabs-nav{margin:0;list-style-type:none;border-bottom:1px solid #ddd;display:flex;justify-content:flex-start;flex-direction:column;padding:0;}  .container--tabs .webVitals-tabs-nav > li{width:100%;display:flex;justify-content:center;align-items:flex-start;}  .container--tabs .webVitals-tabs-nav > li > a{margin-right:2px;line-height:1.42857143;padding:10px;border:1px solid transparent;flex:0 0 100%;font-weight:600;text-decoration:none;background-color:#efefef;color:#555;border:1px solid #ddd;}  .container--tabs .webVitals-tabs-nav > li > a:hover{background-color:#efefef;}  .container--tabs .webVitals-tabs-nav > li.active > a,.container--tabs .webVitals-tabs-nav > li.active > a:hover,.container--tabs .webVitals-tabs-nav > li.active > a:focus{color:#000;cursor:default;background-color:#fff;border-bottom-color:transparent;}  .container--tabs .tab-content{float:left;width:60%;background-color:#fff;}  .container--tabs .tab-content > .tab-pane{display:none;}  .container--tabs .tab-content > .tab-pane.active{display:block;padding:2.5% 3.5%;background-color:#fff;}  .container--tabs .tab-content > .active{display:block;}  .heading-block{font-size:14px;color:#626262;font-style:italic;text-align:left;background:#fff;padding:10px 5px;text-transform: capitalize;border: 2px solid #141414;}  .accordion{display:flex;flex-direction:column;width:100%;height:auto;} .accordion .a-container{display:flex;flex-direction:column;width:100%;padding-bottom:5px;} .accordion .a-container .a-btn{margin:0;position:relative;padding:15px 0px;width:100%;color:#bdbdbd;display:block;font-weight:600;background-color:#262626;cursor:pointer;transition:all 0.5s ease-in-out;box-shadow:0 20px 25px -5px rgba(0, 0, 0, 0.15), 0 10px 10px -5px rgba(0, 0, 0, 0.1) !important;text-align:left;padding-left:10px;box-sizing:border-box;} .accordion .a-container .a-btn:hover{background-color:#141414;} .accordion .a-container .a-btn span{display:block;position:absolute;height:14px;width:14px;right:20px;top:18px;} .accordion .a-container .a-btn span:after{content:"";width:14px;height:2px;border-radius:2px;background-color:#fff;position:absolute;top:6px;} .accordion .a-container .a-btn span:before{content:"";width:14px;height:2px;border-radius:2px;background-color:#fff;position:absolute;top:6px;transform:rotate(90deg);transition:all 0.3s ease-in-out;} .accordion .a-container .a-panel .horizontal-card{padding: 10px 0; border-bottom: 1px solid #ccc;}.accordion .a-container .a-panel{width:100%;color:#262626;transition:all 0.3s ease-in-out;opacity:0;height:auto;max-height:0;overflow:hidden;padding:0px 10px;box-sizing: border-box;background-color: #fff;} .accordion .a-container.active .a-btn{color:#fff;background-color:#141414;} .accordion .a-container.active .a-btn span::before{transform:rotate(0deg);} .accordion .a-container.active .a-panel{opacity:1;max-height:500px;}    .score-info {    position: relative;    display: inline-block;    cursor: pointer;  }  .score-info .tooltiptext {    visibility: hidden;    width: 120px;    background-color: black;    color: #fff;    text-align: center;    border-radius: 6px;    padding: 5px 0;    position: absolute;    z-index: 1;    top: 150%;    left: 50%;    margin-left: -60px;  }  .score-info .tooltiptext::after {    content: "";    position: absolute;    bottom: 100%;    left: 50%;    margin-left: -5px;    border-width: 5px;    border-style: solid;    border-color: transparent transparent black transparent;  }  .score-info.top .tooltiptext {    bottom: 150%;    top: auto;  }  .score-info.top .tooltiptext::after {    top: 100%;    border-color: black transparent transparent transparent;  }  .score-info:hover .tooltiptext {    visibility: visible;text-transform: none;  }</style><script>  const dropdown = document.querySelector("#btn-dropdown");  const targetElement = document.querySelector("#webData");  dropdown.addEventListener("click", function(){    targetElement.classList.toggle("open");  });  </script>';
                   $(webVitals).insertBefore('.editor-GlobalBar-pageTitle');
                   initAcc(".accordion.v2", false); 
                }
  			}).fail(function() {
   				 console.log( " webVitals error" );
  			});
    }
}(jQuery, jQuery(document)));