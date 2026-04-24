<?php

namespace App\Enum;

enum MusicStatus: string
{
    case TO_LEARN = 'to_learn';
    case LEARNING  = 'learning';
    case LEARNT    = 'learnt';
}