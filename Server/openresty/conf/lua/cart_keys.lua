local jwt = require "resty.jwt"
local redis = require "resty.redis"
local cjson = require "cjson"

local auth_header = ngx.var.http_authorization
if not auth_header then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("Missing JWT token")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

local jwt_token = auth_header:match("Bearer%s+(.+)")
if not jwt_token then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("Invalid Authorization format")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

local decoded_token = jwt:verify("yJhY2zA6WxVr8PqWNxQtbk5U4v3iSz1A7ghz6j9kPZJXy9U2w", jwt_token)
if not decoded_token.verified then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("Invalid JWT token")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

local cart_id = decoded_token.payload.cartId
if not cart_id then
    ngx.status = ngx.HTTP_BAD_REQUEST
    ngx.say(cjson.encode({ message = "Cart ID missing in JWT payload" }))
    return ngx.exit(ngx.HTTP_BAD_REQUEST)
end

local red = redis:new()
local ok, err = red:connect("redis", 6379)
if not ok then
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.log(ngx.ERR, "Failed to connect to Redis: " .. (err or "Unknown error"))
    ngx.exec("@error_handler")
    return
end

local key = "shopping_cart:" .. cart_id
local data, err = red:get(key)
if not data then
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.say(cjson.encode({ message = "Failed to retrieve cart data: " .. err }))
    return ngx.exit(ngx.HTTP_INTERNAL_SERVER_ERROR)
end

if data == ngx.null then
    ngx.status = ngx.HTTP_OK
    ngx.header.content_type = "application/json"
    ngx.say("[]")
    return ngx.exit(ngx.HTTP_OK)
end

local decoded_data = cjson.decode(data)
if not decoded_data or type(decoded_data) ~= "table" or next(decoded_data) == nil then
    ngx.status = ngx.HTTP_OK
    ngx.header.content_type = "application/json"
    ngx.say("[]")
    return ngx.exit(ngx.HTTP_OK)
end

ngx.header.content_type = "application/json"
ngx.say(cjson.encode(decoded_data))
ngx.exit(ngx.HTTP_OK)
