#### Manage multiple instances of Spring Batch with Shedlock

My post about this project (English language): https://www.vincenzoracca.com/en/blog/framework/spring/shedlock/
My post about this project (Italian language): https://www.vincenzoracca.com/blog/framework/spring/shedlock/

Shedlock's documentation:\
https://github.com/lukas-krecan/ShedLock/blob/master/README.md

#### Create a MySQL Docker container

##### Step 1: container mysql
docker pull mysql:8.0.1 \
docker run -p 3306:3306 --name sql-container -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.1
##### Step 2: container phppmyadmin

docker pull phpmyadmin/phpmyadmin:latest
docker run --name phpmyadmin-container -d --link sql-container:db -p 8081:80 phpmyadmin/phpmyadmin

console phpmyadmin: http://localhost:8081/ \
user: root \
pass: root