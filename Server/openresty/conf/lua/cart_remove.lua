local jwt = require "resty.jwt"
local redis = require "resty.redis"
local cjson = require "cjson"


 local red = redis:new()
    red:set_timeout(1000)

local ok, err = red:connect("redis", 6379)
    if not ok then
      ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
      ngx.log(ngx.ERR, "Failed to find cart in cache: " .. (err or "Unknown error"))
      ngx.exec("@error_handler")
    return

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
local book_id = ngx.var.uri:match("/api/cart/remove/(.+)")
  local cart_key = "shopping_cart:" .. cart_id
  local version_key = "cartVersion:" .. cart_id

local data, err = red:get(cart_key)
if not data then
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.say(cjson.encode({ message = "Failed to retrieve cart data", error = err }))
    return
end

if data == ngx.null then
   ngx.log(ngx.ERR, "Failed to find cart in cache: " .. (err or "Unknown error"))
   ngx.exec("@error_handler")
return
end

local cart = cjson.decode(data)
local book_found = false

for i, book in ipairs(cart) do
ngx.log(ngx.ERR, "Book: " .. cjson.encode(book))
ngx.log(ngx.ERR, "Comparing book id: " .. book.id .. " with book_id: " .. book_id)
    if book.id == book_id then
        table.remove(cart, i)
        book_found = true
        break
    end
end

if not book_found then
    ngx.status = ngx.HTTP_NOT_FOUND
    ngx.say(cjson.encode({ message = "Book not found in cart" }))
    return
end

ngx.log(ngx.ERR, "Cart state before encoding: " .. cjson.encode(cart))

local updated_data = cjson.encode(cart)
local ok, err = red:set(cart_key, updated_data)
if not ok then
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.say(cjson.encode({ message = "Failed to update cart data", error = err }))
    return
end

red:incr(version_key)
red:expire(cart_key, 70)

ngx.status = ngx.HTTP_OK
ngx.say(cjson.encode({ message = "Book removed from cart successfully" }))