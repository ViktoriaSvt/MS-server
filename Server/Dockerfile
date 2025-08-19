FROM openresty/openresty:1.21.4.1-0-focal


RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    make \
    libssl-dev \
    libpcre3-dev

RUN luarocks install lua-resty-redis
RUN luarocks install lua-cjson
RUN luarocks install lua-resty-jwt


COPY ./openresty/luarocks/.luarocks/share/lua/5.1 /usr/local/openresty/lua


COPY ./openresty/conf/nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
COPY ./openresty/conf/lua/cart_keys.lua /usr/local/openresty/lua/cart_keys.lua
COPY ./openresty/conf/lua/cart.lua /usr/local/openresty/lua/cart.lua
COPY ./openresty/conf/lua/cart_remove.lua /usr/local/openresty/lua/cart_remove.lua


ENV LUA_PATH="/usr/local/openresty/lua/?.lua;;"

EXPOSE 8081

CMD ["openresty", "-g", "daemon off;"]
