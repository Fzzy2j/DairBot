package me.fzzy.dair.util


class SmashQL {

    fun request() {
        val schema = "query EventSets(\$eventId: ID!, \$page:Int!, \$perPage:Int!){\n" +
                "  event(id:\$eventId){\n" +
                "    id\n" +
                "    name\n" +
                "    sets(\n" +
                "      page: \$page\n" +
                "      perPage: \$perPage\n" +
                "      sortType: STANDARD\n" +
                "    ){\n" +
                "      pageInfo{\n" +
                "        total\n" +
                "      }\n" +
                "      nodes{\n" +
                "        id\n" +
                "        slots{\n" +
                "          id\n" +
                "          entrant{\n" +
                "            id\n" +
                "            name\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"

    }

}