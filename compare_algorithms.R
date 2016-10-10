suppressPackageStartupMessages({
  library(dplyr)
  library(magrittr)
  library(ggplot2)
  library(reshape2)
})

algo_data = read.csv('results.csv')

ggplot2::ggplot(algo_data) +
  ggplot2::geom_point(aes(x = mk_blind, y = cost_blind)) +
  ggplot2::geom_point(aes(x = mk_minmin, y = cost_minmin)) +
  ggplot2::geom_point(aes(x = mk_maxmin, y = cost_maxmin)) +
  ggplot2::geom_point(aes(x = mk_myopic, y = cost_myopic))

algo_mk = reshape2::melt(algo_data, 
                         id.vars = "wf_num", 
                         measure.vars = c("mk_blind", "mk_maxmin", "mk_minmin", "mk_myopic"),
                         variable.name = "mk") %>%
  dplyr::mutate(algo_name = sapply(stringr::str_split(mk, "_"), function(x) x[[2]])) %>%
  dplyr::select(wf_num, algo_name, value) %>%
  dplyr::rename(mk = value)

algo_cost = reshape2::melt(algo_data, 
                           id.vars = "wf_num", 
                           measure.vars = c("cost_blind", "cost_maxmin", "cost_minmin", "cost_myopic"),
                           variable.name = "cost") %>%
  dplyr::mutate(algo_name = sapply(stringr::str_split(cost, "_"), function(x) x[[2]])) %>%
  dplyr::select(wf_num, algo_name, value) %>%
  dplyr::rename(cost = value)


algo_shape = algo_mk %>%
  dplyr::inner_join(algo_cost, by=c("wf_num", "algo_name"))

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_point(aes( x = mk, y = cost, colour = algo_name))

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_bar(aes( x = wf_num, y = mk, fill = algo_name), stat = "identity", position="dodge")

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_bar(aes( x = wf_num, y = cost, fill = algo_name), stat = "identity", position="dodge")


algo_summ = algo_shape %>%
  dplyr::group_by(algo_name) %>%
  dplyr::summarise(
    mk_mean = mean(mk),
    mk_sd = sd(mk),
    cost_mean = mean(cost),
    cost_sd = sd(cost)
  )

algo_data_absolute = algo_data %>%
   dplyr::filter(mk_blind < mk_myopic & mk_blind < mk_minmin & mk_blind < mk_maxmin)
  
algo_data_some = algo_data %>%
  dplyr::filter(mk_blind <= mk_myopic & mk_blind <= mk_minmin & mk_blind <= mk_maxmin)
