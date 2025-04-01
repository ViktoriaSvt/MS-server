local redis = require "resty.redis"
local cjson = require "cjson"
local jwt = require "resty.jwt"

local function process_cart(cart_id, book)
    local red = redis:new()


local ok, err = red:connect("redis", 6379)
if not ok then
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.log(ngx.ERR, "Failed to connect to redis: " .. (err or "Unknown error"))
ngx.exec("@error_handler")
return
end

    local cart_key = "shopping_cart:" .. cart_id
    local version_key = "cartVersion:" .. cart_id

   ngx.log(ngx.ERR, "Failed to find cart in cache: " .. (cart_key or "Unknown error"))

    local cart, err = red:get(cart_key)
    if cart == ngx.null then
    ngx.log(ngx.ERR, "Failed to find cart in cache: " .. (err or "Unknown error"))
ngx.exec("@error_handler")
return
    end

    cart = cjson.decode(cart)
    table.insert(cart, book)

    red:set(cart_key, cjson.encode(cart))
    red:incr(version_key)
    red:expire(cart_key, 70)

    ngx.status = ngx.HTTP_OK
    ngx.say(cjson.encode({ message = "Book added to cart in Redis" }))
end

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

ngx.req.read_body()
local data = ngx.req.get_body_data()

if not data then
    ngx.status = ngx.HTTP_BAD_REQUEST
    ngx.say("Missing request body")
    return ngx.exit(ngx.HTTP_BAD_REQUEST)
end

local ok, book = pcall(cjson.decode, data)
if not ok or not book then
    ngx.status = ngx.HTTP_BAD_REQUEST
    ngx.say("Invalid JSON in request body")
    return ngx.exit(ngx.HTTP_BAD_REQUEST)
end

if not book.id or not book.title then
    ngx.status = ngx.HTTP_BAD_REQUEST
    ngx.say("Invalid book object")
    return ngx.exit(ngx.HTTP_BAD_REQUEST)
end

process_cart(cart_id, book)

ngx.status = ngx.HTTP_OK
ngx.say(cjson.encode({ message = "Book added to cart successfully" }))