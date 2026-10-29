FROM php:8.2-apache

RUN apt-get update && apt-get install -y \
    git \
    unzip \
    libpq-dev \
    && docker-php-ext-install pdo pdo_pgsql

COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html

COPY . .

ENV APP_ENV=prod

RUN composer install --no-dev --optimize-autoloader

RUN mkdir -p var/cache var/log public/xml public/audio \
    && chown -R www-data:www-data var/ public/xml public/audio \
    && chmod -R 775 var/ public/xml public/audio

RUN APP_ENV=prod php bin/console cache:warmup

RUN a2enmod rewrite

COPY docker/apache.conf /etc/apache2/sites-available/000-default.conf

EXPOSE 80
