# diurnal-dbi-service

### Tech stack:

<ol>
<li>Java 8 (as Heroku doesn't support higher buildpacks by default)</li>
<li>Spring-Boot</li>
<li>Heroku</li>
<li>Postgres - for prod</li>
<li>CockroachDB - for local dev. CRDB is built on postgres. <a href="https://www.cockroachlabs.com">https://www.cockroachlabs.com/</a></li>
<li>Spring fox - Swagger</li>
<li>Lombok</li>
<li>Gson</li>
<li>Protobuf</li>
<li>Apache Commons Lang3</li>
<li>Assertj</li>
</ol> 

### Important notepoints:

<ul>
<li>Database is using timestamptz to store all time in UTC by default. Whenever the java process will read it from DB, it will get the time converted into the JVM time.</li>
</ul> 