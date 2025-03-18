---
license: cc0-1.0
task_categories:
- sentence-similarity
- summarization
- feature-extraction
tags:
- games
- video-games
---
<p align="center">
  <img src="https://cdn-uploads.huggingface.co/production/uploads/65e3c559d26b426e3e1994f8/e85CmtDucO_FQ-W5h1RTB.png" />
</p>

<div align="center">
  
  ![visitors](https://visitor-badge.laobi.icu/badge?page_id=atalaydenknalbant/rawg-games-dataset)

</div>

<h6 style="text-align: center;"><strong>Description</strong></h6>
<p style="text-align: center;">
  <strong>RAWG Games Dataset</strong> video game records data gathered directly from the RAWG API.
  It includes essential fields such as game id, title, release date, rating, genres, platforms, descriptive tags, 
  Metacritic score, developers, publishers, playtime, and a detailed description. The data was collected to support 
  studies, trend analysis, and insights into the gaming industry. Each field is aligned with the specifications provided in the RAWG API documentation.
</p>

<p style="text-align: center;"><strong>Latest Update: February 14, 2025</strong></p>

<h6 style="text-align: center;"><strong>Acknowledgements</strong></h6>

<p style="text-align: center;">
  Grateful to <a href="https://rawg.io/apidocs">RAWG</a> for data API.
</p>

<table align="center" border="1" cellpadding="10" cellspacing="0">
  <tr>
    <th>Field</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>id</td>
    <td>A unique identifier for each game, serving as the primary key to reference detailed game data via the API.</td>
  </tr>
  <tr>
    <td>name</td>
    <td>The official title of the game.</td>
  </tr>
  <tr>
    <td>released</td>
    <td>The release date of the game, typically in the YYYY-MM-DD format.</td>
  </tr>
  <tr>
    <td>rating</td>
    <td>An aggregated score based on player reviews, computed on a standardized scale reflecting user opinions.</td>
  </tr>
  <tr>
    <td>genres</td>
    <td>A list of genre objects categorizing the game (e.g., Action, Adventure, RPG).</td>
  </tr>
  <tr>
    <td>platforms</td>
    <td>An array of platform objects that indicate on which systems the game is available (e.g., PC, PlayStation, Xbox).</td>
  </tr>
  <tr>
    <td>tags</td>
    <td>A collection of descriptive keyword tags (e.g., multiplayer, indie).</td>
  </tr>
  <tr>
    <td>metacritic</td>
    <td>A numerical score derived from Metacritic reviews (usually ranging from 0 to 100).</td>
  </tr>
  <tr>
    <td>developers</td>
    <td>The individuals or companies responsible for creating the game.</td>
  </tr>
  <tr>
    <td>publishers</td>
    <td>Entities that market and distribute the game.</td>
  </tr>
  <tr>
    <td>playtime</td>
    <td>An estimate of the average time (in hours) that players spend engaging with the game.</td>
  </tr>
  <tr>
    <td>description</td>
    <td>A detailed narrative of the game, providing in-depth information about gameplay, plot, mechanics, and overall context.</td>
  </tr>
</table>
