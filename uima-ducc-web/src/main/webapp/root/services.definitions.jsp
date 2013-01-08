<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<html>
<head>
  <link rel="shortcut icon" href="ducc.ico" />
  <title>ducc-mon</title>
  <meta http-equiv="CACHE-CONTROL" content="NO-CACHE">
  <script src="opensources/jquery-1.4.2.js" type="text/javascript"></script>
  <script type="text/javascript" language="javascript" src="opensources/DataTables-1.9.1/media/js/jquery.dataTables.min.js"></script>
  <script src="opensources/jgrowl/jquery.jgrowl.js" type="text/javascript"></script>
  <link rel="stylesheet" href="opensources/jgrowl/jquery.jgrowl.css" type="text/css"/>
  <link href="opensources/jquery-ui-1.8.4/gallery/jquery-ui-themes-1.8.4/themes/redmond/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <script src="opensources/jquery-ui-1.8.4/ui/minified/jquery-ui.min.js"></script>
  <link href="opensources/navigation/menu.css" rel="stylesheet" type="text/css">
  <script src="js/ducc.js"></script>
  <link href="ducc.css" rel="stylesheet" type="text/css">
  <script type="text/javascript" charset="utf-8">
	var oTable;
	$(document).ready(function() {
		oTable = $('#services-definitions-table').dataTable( {
			"bProcessing": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "600px",
       		"bInfo": false,
			"sAjaxSource": "ducc-servlet/json-services-definitions-data",
			aaSorting: [],
			"fnRowCallback"  : function(nRow,aData,iDisplayIndex) {
                             		$('td:eq(0)' , nRow).css( "text-align", "right" );
                             		$('td:eq(2)' , nRow).css( "text-align", "right" );
                             		$('td:eq(5)' , nRow).css( "text-align", "right" );
                             		return nRow;
			},
		} );
	} );
	</script>
</head>
<body onload="ducc_init('services-definitions');">

<!-- ####################### common ######################## -->
<div class="page">
<!-- *********************** table ************************* -->
<table class="heading">
<!-- *********************** row *************************** -->
<tr class="heading">
<!-- *********************** column ************************ -->
<td valign="middle" align="center">
<div>
<ul id="accordion">
<li><a href="jobs.html">Jobs</a></li>
<ul></ul>
<li><a href="reservations.html">Reservations</a></li>
<ul></ul>
<li>Services</li>
<ul>
<li><a href="services.definitions.html">Definitions</a></li>
<li><a href="services.deployments.html">Deployments</a></li>
</ul>
<li>System</li>
<ul>
<li><a href="system.administration.html">Administration</a></li>
<li><a href="system.classes.html">Classes</a></li>
<li><a href="system.daemons.html">Daemons</a></li>
<li><a href="system.duccbook.html" target="_duccbook">DuccBook</a></li>
<li><a href="system.machines.html">Machines</a></li>
</ul>
<%@ include file="site.jsp" %>
</ul>
</div>
<!-- *********************** column ************************ -->
<td valign="middle" align="center">
<div id="refreshbutton">
<input type="image" onClick="ducc_refresh('services-definitions')" title="Refresh" alt="Refresh" src="opensources/images/1284662827_refresh.png">
</div>
<br>
<table>
<tr>
<td align="left">
<form name="duccform">
<fieldset>
<legend>Refresh</legend>
<input type="radio" name="refresh" value="manual"    checked onClick="ducc_put_cookie('ducc_refresh_mode','manual'   )" /> Manual
<br>
<input type="radio" name="refresh" value="automatic"         onClick="ducc_put_cookie('ducc_refresh_mode','automatic')" /> Automatic
</fieldset>
</form>
</table>
</td>
<!-- *********************** column ************************ -->
<td valign="middle" align="center">
<h2>
<span class="idtitle" id="identity"></span>
</h2>
<form name="form_selectors">
<table>
<tr>
<td valign="top" align="right">Updated:
<td valign="top"><span class="timestamptitle" id="timestamp_area"></span>
<tr>
<td valign="top" align="right">Authentication:
<td valign="top"><span class="authenticationtitle" id="authentication_area">?</span>
<tr>
<td valign="top" align="right">Max Records:
<td valign="top"><input type="text" size="8" id="maxrecs_input" value="default">
<tr style="display:none">
<td valign="top" align="right">
<select id="users_select">
<option value="active+include">active+include</option>
<option value="active+exclude">active+exclude</option>
<option value="include">include</option>
<option value="exclude">exclude</option>
</select>
Users:
<td valign="top"><input type="text" size="16" id="users_input" value="default">
<tr>
<td>
<input type="hidden" id="users_select_input" value="default">
</table>
</form>
</td>
<!-- *********************** column ************************ -->
<td valign="middle" align="center">
<h2><span class="subtitle">Services Definitions</span></h2>
<img src="opensources/images/120px-Head_of_a_white_domesticated_duck.jpg" style="border:3px solid #ffff7a" alt="logo">
</td>
<!-- *********************** column ************************ -->
<td valign="middle" align="center">
<table>
<tr>
<td valign="middle" align="right">
<span id="login_link_area"></span>
 |
<span id="logout_link_area"><a href="logout.html" onclick="var newWin = window.open(this.href,'child','height=600,width=475,scrollbars'); newWin.focus(); return false;">Logout</a></span>
 |
<span id="duccbook_link_area"></span><a href="system.duccbook.html" target="_duccbook">DuccBook</a>
<tr>
<td valign="middle" align="center">
<div>
<br>
<h2><span class="title">ducc-mon</span></h2>
<span class="title_acronym">D</span><span class="title">istributed</span>
<span class="title_acronym">U</span><span class="title">IMA</span>
<span class="title_acronym">C</span><span class="title">luster</span>
<span class="title_acronym">C</span><span class="title">omputing</span>
<span class="title_acronym">Mon</span><span class="title">itor</span>
<br>
<i>Version: <span class="version" id="version"></span></i>
<br>
<br>
<table>
<tr>
<td valign="middle" align="left">
<small>Copyright &copy 2012 The Apache Software Foundation</small>
<tr>
<td valign="middle" align="left">
<small>Copyright &copy 2011, 2012 International Business Machines Corporation</small>
</table>
</div> 
</table>
<br>
</td>
</table>
<!-- *********************** /table ************************ -->
<!-- ####################### /common ####################### -->
<!-- @@@@@@@@@@@@@@@@@@@@@@@ unique @@@@@@@@@@@@@@@@@@@@@@@@ -->
	<table id="services-definitions-table" width="100%">
   	    <caption><b>Services Definitions List</b><br><i><small>click column heading to sort</small></i></caption>
			<thead>
			<tr class="ducc-header">
			<th align="left" title="The service Id">Id</th>
			<th align="left" title="The service endpoint">Endpoint</th>
			<th align="left" title="The service number of instances">Instances</th>
			<th align="left" title="The service owning user">Owning User</th>
			<th align="left" title="The service scheduling class">Scheduling<br>Class</th>
			<th align="left" title="The service process memory size (GB)">Size</th>
			<th align="left" title="The service description">Description</th>
			</tr>
			</thead>
			<tbody id="services_definitions_list_area">
   	  		</tbody>
   	    </table>
<!-- @@@@@@@@@@@@@@@@@@@@@@@ /unique @@@@@@@@@@@@@@@@@@@@@@@@ -->
<!-- ####################### common ######################### -->
</div>
<!-- ####################### /common ####################### -->
		
<script src="opensources/navigation/menu.js"></script>
</body>
</html>
