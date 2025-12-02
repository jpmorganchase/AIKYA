##########################################################################
##########################################################################
################## START: FED-LEARN-UI IMAGE DEFINITION ##################
##########################################################################

# Stage 1: Build the React application
FROM node:22-alpine AS build

ARG REACT_APP_BANK
ARG REACT_APP_ENV
ARG GENERATE_SOURCEMAP
ARG DISABLE_ESLINT_PLUGIN
# Build the application with optimizations
ARG GENERATE_SOURCEMAP
ARG BUILD_SUB_CONFIG

ENV REACT_APP_ENV=$REACT_APP_ENV
ENV GENERATE_SOURCEMAP=$GENERATE_SOURCEMAP
ENV DISABLE_ESLINT_PLUGIN=$DISABLE_ESLINT_PLUGIN
ENV NODE_OPTIONS=--max_old_space_size=4096
ENV REACT_APP_BANK=$REACT_APP_BANK
ENV GENERATE_SOURCEMAP=$GENERATE_SOURCEMAP
ENV BUILD_SUB_CONFIG=$BUILD_SUB_CONFIG

# Set working directory
WORKDIR /app

# Copy package files first for better layer caching
COPY package.json package-lock.json ./

# Install dependencies with npm ci for faster, reproducible builds
RUN npm ci --legacy-peer-deps --prefer-offline --no-audit --no-fund && \
    npm cache clean --force

# Copy source files
COPY . .


RUN npm run build:${REACT_APP_ENV}

# Stage 2: Serve the static files using nginx
FROM nginx:alpine

# Install brotli tools for pre-compression (not the nginx module)
RUN apk add --no-cache brotli

# Use a build argument for the NGINX config and set it as an environment variable
COPY k8/nginx.conf.template /etc/nginx/templates/

# Copy the build output from builder stage to nginx web directory
COPY --from=build /app/build /usr/share/nginx/html

# Pre-compress static assets for faster serving
RUN find /usr/share/nginx/html -type f \
    \( -name '*.html' -o -name '*.js' -o -name '*.css' -o -name '*.svg' -o -name '*.json' \) \
    -exec sh -c 'gzip -9 -k -f "$1" && brotli -9 -k -f "$1"' _ {} \;

# Expose the necessary port
EXPOSE 80

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]

##########################################################################
##########################################################################
################## END: FED-LEARN-UI IMAGE DEFINITION ##################
##########################################################################
