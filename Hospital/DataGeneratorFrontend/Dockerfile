FROM node:18-alpine as build
WORKDIR /app
COPY package.json ./

RUN npm install --verbose
COPY . ./
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/build /app
CMD ["serve", "-s", ".", "-l", "3001"]